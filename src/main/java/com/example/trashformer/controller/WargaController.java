package com.example.trashformer.controller;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trashformer.model.KategoriSampah;
import com.example.trashformer.model.Setoran;
import com.example.trashformer.model.StatusPembayaran;
import com.example.trashformer.model.StatusTopup;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.KategoriSampahRepository;
import com.example.trashformer.repository.SetoranRepository;
import com.example.trashformer.repository.UserRepository;
import com.example.trashformer.service.BankSampahService;
import com.example.trashformer.service.SetoranService;
import com.example.trashformer.service.UserService;

@Controller
@RequestMapping("/warga")
public class WargaController {

    private final UserService userService;
    private final SetoranService setoranService;
    private final SetoranRepository setoranRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final KategoriSampahRepository kategoriSampahRepository;
    private final BankSampahService bankSampahService;

    @Value("${app.upload.dir:uploads/bukti_pembayaran}")
    private String uploadDir;

    public WargaController(UserService userService,
                           SetoranService setoranService,
                           SetoranRepository setoranRepository,
                           PasswordEncoder passwordEncoder,
                           UserRepository userRepository,
                           KategoriSampahRepository kategoriSampahRepository,
                           BankSampahService bankSampahService) {
        this.userService = userService;
        this.setoranService = setoranService;
        this.setoranRepository = setoranRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.kategoriSampahRepository = kategoriSampahRepository;
        this.bankSampahService = bankSampahService;
    }

    private void addUserToModel(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("nama", user.getNama());
                model.addAttribute("role", user.getRole().name());
            }
        }
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.getUserByUsername(authentication.getName()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User warga = getCurrentUser(authentication);
        if (warga != null) {
            Long wargaId = warga.getId();
            List<Setoran> allSetoran = setoranRepository.findByWargaIdOrderByCreatedAtDesc(wargaId);

            List<Setoran> sampahList = allSetoran.stream()
                    .filter(s -> s.getJenisSetoran() == com.example.trashformer.model.JenisSetoran.SAMPAH)
                    .toList();

            double totalBerat = sampahList.stream()
                    .mapToDouble(s -> s.getBeratKg() != null ? s.getBeratKg().doubleValue() : 0.0)
                    .sum();

            model.addAttribute("totalSampah", (long) totalBerat);
            model.addAttribute("totalSetoran", sampahList.size());
            model.addAttribute("saldo", bankSampahService.getSaldo(wargaId));

            List<Setoran> recent = sampahList.size() > 5 ? sampahList.subList(0, 5) : sampahList;
            model.addAttribute("recentSetoran", recent);

            int currentYear = LocalDate.now().getYear();
            List<Object[]> bulanData = setoranRepository.sumBeratPerBulanByWarga(wargaId, currentYear);
            double[] bulanValues = new double[12];
            for (Object[] row : bulanData) {
                int month = ((Number) row[0]).intValue();
                double berat = ((Number) row[1]).doubleValue();
                if (month >= 1 && month <= 12) {
                    bulanValues[month - 1] = berat;
                }
            }
            model.addAttribute("bulanData", bulanValues);
        }
        return "warga/dashboard";
    }

    @GetMapping("/profil")
    public String profil(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User user = getCurrentUser(authentication);
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "warga/profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@RequestParam String nama,
                                @RequestParam(required = false) String alamat,
                                @RequestParam(required = false) String noTelepon,
                                Authentication authentication,
                                RedirectAttributes redirectAttrs) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                redirectAttrs.addFlashAttribute("error", "User tidak ditemukan");
                return "redirect:/warga/profil";
            }
            user.setNama(nama.trim());
            if (alamat != null) user.setAlamat(alamat.trim());
            if (noTelepon != null) user.setNoTelepon(noTelepon.trim());
            userRepository.save(user);
            redirectAttrs.addFlashAttribute("success", "Profil berhasil diupdate");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengupdate profil");
        }
        return "redirect:/warga/profil";
    }

    @PostMapping("/profil/ganti-password")
    public String gantiPassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Authentication authentication,
                                RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            redirectAttrs.addFlashAttribute("error", "User tidak ditemukan");
            return "redirect:/warga/profil";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttrs.addFlashAttribute("error", "Password saat ini salah");
            return "redirect:/warga/profil";
        }

        if (newPassword.trim().length() < 6) {
            redirectAttrs.addFlashAttribute("error", "Password baru minimal 6 karakter");
            return "redirect:/warga/profil";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "Konfirmasi password tidak cocok");
            return "redirect:/warga/profil";
        }

        try {
            userService.resetPassword(user.getId(), newPassword.trim());
            redirectAttrs.addFlashAttribute("success", "Password berhasil diganti");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengganti password");
        }
        return "redirect:/warga/profil";
    }

    @GetMapping("/riwayat")
    public String riwayatSampah(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User warga = getCurrentUser(authentication);
        if (warga != null) {
            List<Setoran> allSetoran = setoranRepository.findByWargaIdOrderByCreatedAtDesc(warga.getId());
            List<Setoran> sampahList = allSetoran.stream()
                    .filter(s -> s.getJenisSetoran() == com.example.trashformer.model.JenisSetoran.SAMPAH)
                    .toList();
            List<Setoran> uangList = allSetoran.stream()
                    .filter(s -> s.getJenisSetoran() == com.example.trashformer.model.JenisSetoran.UANG)
                    .toList();
            model.addAttribute("setoranList", sampahList);
            model.addAttribute("setoranUangList", uangList);
        }
        return "warga/riwayat";
    }

    @GetMapping("/laporan")
    public String laporanForm(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User warga = getCurrentUser(authentication);
        if (warga != null) {
            model.addAttribute("alamat", warga.getAlamat() != null ? warga.getAlamat() : "");
            model.addAttribute("saldo", bankSampahService.getSaldo(warga.getId()));
        }
        List<KategoriSampah> kategoriList = kategoriSampahRepository.findAll();
        model.addAttribute("listKategori", kategoriList);
        return "warga/laporan";
    }

    @PostMapping("/laporan/simpan")
    @Transactional
    public String laporanSimpan(@RequestParam("kategoriIds") List<Long> kategoriIds,
                                @RequestParam("beratKgs") List<BigDecimal> beratKgs,
                                @RequestParam(required = false) String alamatJemput,
                                @RequestParam(required = false) String catatanTambahan,
                                @RequestParam(value = "buktiPembayaran", required = false) MultipartFile buktiPembayaran,
                                @RequestParam(defaultValue = "false") boolean gunakanSaldo,
                                @RequestParam(required = false) BigDecimal jumlahSaldo,
                                Authentication authentication,
                                RedirectAttributes redirectAttrs) {
        try {
            User warga = getCurrentUser(authentication);
            if (warga == null) {
                redirectAttrs.addFlashAttribute("error", "User tidak ditemukan");
                return "redirect:/warga/laporan";
            }

            if (kategoriIds == null || kategoriIds.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Pilih minimal satu jenis sampah");
                return "redirect:/warga/laporan";
            }

            if (beratKgs == null || kategoriIds.size() != beratKgs.size()) {
                redirectAttrs.addFlashAttribute("error", "Data berat tidak valid");
                return "redirect:/warga/laporan";
            }

            Map<Long, KategoriSampah> kategoriMap = kategoriSampahRepository.findAllById(kategoriIds).stream()
                    .collect(Collectors.toMap(KategoriSampah::getId, k -> k));

            boolean allDaurUlang = kategoriIds.stream()
                    .map(kategoriMap::get)
                    .allMatch(k -> k != null && Boolean.TRUE.equals(k.getIsDaurUlang()));

            BigDecimal totalBiaya = BigDecimal.ZERO;
            for (int i = 0; i < kategoriIds.size(); i++) {
                KategoriSampah k = kategoriMap.get(kategoriIds.get(i));
                if (k != null && !Boolean.TRUE.equals(k.getIsDaurUlang())) {
                    BigDecimal harga = k.getHargaPerKg() != null ? k.getHargaPerKg() : BigDecimal.ZERO;
                    totalBiaya = totalBiaya.add(beratKgs.get(i).multiply(harga));
                }
            }

            BigDecimal saldoDigunakan = BigDecimal.ZERO;
            if (gunakanSaldo && jumlahSaldo != null && jumlahSaldo.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentSaldo = bankSampahService.getSaldo(warga.getId());
                if (jumlahSaldo.compareTo(currentSaldo) > 0) {
                    redirectAttrs.addFlashAttribute("error", "Saldo bank sampah tidak mencukupi");
                    return "redirect:/warga/laporan";
                }
                if (jumlahSaldo.compareTo(totalBiaya) > 0) {
                    redirectAttrs.addFlashAttribute("error", "Jumlah saldo melebihi total biaya");
                    return "redirect:/warga/laporan";
                }
                saldoDigunakan = jumlahSaldo;
            }

            BigDecimal sisaBayar = totalBiaya.subtract(saldoDigunakan);
            boolean lunasDenganSaldo = sisaBayar.compareTo(BigDecimal.ZERO) <= 0;

            String fileName = null;
            if (buktiPembayaran != null && !buktiPembayaran.isEmpty()) {
                String originalName = buktiPembayaran.getOriginalFilename();
                if (originalName != null && !originalName.isEmpty()) {
                    int dotIndex = originalName.lastIndexOf('.');
                    if (dotIndex <= 0) {
                        redirectAttrs.addFlashAttribute("error", "File bukti pembayaran tidak memiliki ekstensi");
                        return "redirect:/warga/laporan";
                    }
                    String extension = originalName.substring(dotIndex).toLowerCase();
                    Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp");
                    if (!allowedExtensions.contains(extension)) {
                        redirectAttrs.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, GIF, atau BMP");
                        return "redirect:/warga/laporan";
                    }
                    fileName = UUID.randomUUID().toString() + extension;
                    Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
                    Files.createDirectories(uploadPath);
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(buktiPembayaran.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (!allDaurUlang && fileName == null && !lunasDenganSaldo) {
                redirectAttrs.addFlashAttribute("error", "Bukti pembayaran wajib diunggah untuk sisa biaya sebesar Rp " + sisaBayar);
                return "redirect:/warga/laporan";
            }

            if (saldoDigunakan.compareTo(BigDecimal.ZERO) > 0) {
                bankSampahService.debitSaldo(warga.getId(), saldoDigunakan,
                        "Pembayaran setoran sampah (Rp " + saldoDigunakan + " dari Rp " + totalBiaya + ")");
            }

            boolean hasDaurUlang = false;
            boolean hasBiasa = false;
            for (int i = 0; i < kategoriIds.size(); i++) {
                KategoriSampah kategori = kategoriMap.get(kategoriIds.get(i));
                boolean isDaurUlang = kategori != null && Boolean.TRUE.equals(kategori.getIsDaurUlang());
                if (isDaurUlang) {
                    hasDaurUlang = true;
                    setoranService.createSetoranSampahWarga(
                            warga.getId(), kategoriIds.get(i), beratKgs.get(i),
                            alamatJemput, catatanTambahan, null);
                } else {
                    hasBiasa = true;
                    String bukti = lunasDenganSaldo ? null : fileName;
                    Setoran setoran = setoranService.createSetoranSampahWarga(
                            warga.getId(), kategoriIds.get(i), beratKgs.get(i),
                            alamatJemput, catatanTambahan, bukti);
                    if (lunasDenganSaldo) {
                        setoran.setStatusPembayaran(StatusPembayaran.DISETUJUI);
                        setoranRepository.save(setoran);
                    }
                }
            }

            String msg;
            if (lunasDenganSaldo && saldoDigunakan.compareTo(BigDecimal.ZERO) > 0) {
                if (hasDaurUlang) {
                    msg = "Pembayaran lunas menggunakan saldo bank sampah. Setoran daur ulang menunggu verifikasi petugas";
                } else {
                    msg = "Pembayaran lunas menggunakan saldo bank sampah. Petugas akan segera menjemput sampah Anda";
                }
            } else if (hasDaurUlang && !hasBiasa) {
                msg = "Setoran daur ulang berhasil dikirim, menunggu verifikasi petugas";
            } else if (hasDaurUlang) {
                msg = "Laporan berhasil dikirim. Setoran biasa menunggu verifikasi pembayaran, setoran daur ulang menunggu verifikasi petugas";
            } else {
                msg = "Laporan setoran berhasil dikirim, menunggu verifikasi pembayaran oleh petugas";
            }
            redirectAttrs.addFlashAttribute("success", msg);
            return "redirect:/warga/dashboard";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengirim laporan: " + e.getMessage());
            return "redirect:/warga/laporan";
        }
    }

    @GetMapping("/bank-saldo")
    public String bankSaldo(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User warga = getCurrentUser(authentication);
        if (warga != null) {
            model.addAttribute("saldo", bankSampahService.getSaldo(warga.getId()));
            model.addAttribute("riwayatSaldo", bankSampahService.getRiwayatSaldo(warga.getId()));
            model.addAttribute("riwayatPenarikan", bankSampahService.getRiwayatPenarikan(warga.getId()));
            model.addAttribute("riwayatTopup", bankSampahService.getRiwayatTopup(warga.getId()));
        }
        return "warga/bank-saldo";
    }

    @PostMapping("/bank-saldo/topup")
    public String topupSaldo(@RequestParam BigDecimal jumlah,
                             @RequestParam(required = false) String catatan,
                             Authentication authentication,
                             RedirectAttributes redirectAttrs) {
        try {
            User warga = getCurrentUser(authentication);
            if (warga == null) {
                redirectAttrs.addFlashAttribute("error", "User tidak ditemukan");
                return "redirect:/warga/bank-saldo";
            }
            bankSampahService.requestTopup(warga.getId(), jumlah, catatan);
            redirectAttrs.addFlashAttribute("success", "Permintaan topup saldo berhasil dikirim, menunggu persetujuan admin");
            return "redirect:/warga/bank-saldo";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengirim permintaan: " + e.getMessage());
            return "redirect:/warga/bank-saldo";
        }
    }

    @PostMapping("/bank-saldo/tarik")
    public String tarikSaldo(@RequestParam BigDecimal jumlah,
                             @RequestParam(required = false) String catatan,
                             Authentication authentication,
                             RedirectAttributes redirectAttrs) {
        try {
            User warga = getCurrentUser(authentication);
            if (warga == null) {
                redirectAttrs.addFlashAttribute("error", "User tidak ditemukan");
                return "redirect:/warga/bank-saldo";
            }
            bankSampahService.requestPenarikan(warga.getId(), jumlah, catatan);
            redirectAttrs.addFlashAttribute("success", "Permintaan penarikan saldo berhasil dikirim, menunggu persetujuan petugas");
            return "redirect:/warga/bank-saldo";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengirim permintaan: " + e.getMessage());
            return "redirect:/warga/bank-saldo";
        }
    }
}
