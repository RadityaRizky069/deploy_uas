package com.example.trashformer.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trashformer.model.KategoriSampah;
import com.example.trashformer.model.Role;
import com.example.trashformer.model.Setoran;
import com.example.trashformer.model.StatusPembayaran;
import com.example.trashformer.model.StatusPenarikan;
import com.example.trashformer.model.StatusPenjemputan;
import com.example.trashformer.model.StatusSetoran;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.KategoriSampahRepository;
import com.example.trashformer.repository.SetoranRepository;
import com.example.trashformer.service.BankSampahService;
import com.example.trashformer.service.SetoranService;
import com.example.trashformer.service.UserService;

@Controller
@RequestMapping("/petugas")
public class PetugasController {

    private final UserService userService;
    private final SetoranService setoranService;
    private final KategoriSampahRepository kategoriSampahRepository;
    private final SetoranRepository setoranRepository;
    private final BankSampahService bankSampahService;

    public PetugasController(UserService userService,
                             SetoranService setoranService,
                             KategoriSampahRepository kategoriSampahRepository,
                             SetoranRepository setoranRepository,
                             BankSampahService bankSampahService) {
        this.userService = userService;
        this.setoranService = setoranService;
        this.kategoriSampahRepository = kategoriSampahRepository;
        this.setoranRepository = setoranRepository;
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
        User petugas = getCurrentUser(authentication);
        if (petugas != null) {
            List<Setoran> allByPetugas = setoranService.getSetoranByPetugas(petugas.getId());
            long totalVerified = allByPetugas.size();
            long totalMenunggu = setoranRepository.countByStatus(StatusSetoran.MENUNGGU);
            double totalBerat = allByPetugas.stream()
                    .filter(s -> s.getStatus() == StatusSetoran.DITERIMA)
                    .mapToDouble(s -> s.getBeratKg() != null ? s.getBeratKg().doubleValue() : 0.0)
                    .sum();

            long pembayaranMenunggu = setoranRepository.countByStatusPembayaran(StatusPembayaran.MENUNGGU_VERIFIKASI);

            model.addAttribute("setoranHariIni", totalMenunggu + totalVerified);
            model.addAttribute("wargaDibantu", totalVerified);
            model.addAttribute("totalSampah", (long) totalBerat);
            model.addAttribute("pembayaranMenunggu", pembayaranMenunggu);

            List<Setoran> allSetoran = setoranService.getAllSetoranSampah();
            List<Setoran> recent = allSetoran.size() > 5 ? allSetoran.subList(0, 5) : allSetoran;
            model.addAttribute("recentSetoran", recent);
        }
        return "petugas/dashboard";
    }

    @GetMapping("/warga")
    public String listWarga(@RequestParam(required = false) String search,
                            Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<User> wargaList;
        if (search != null && !search.trim().isEmpty()) {
            wargaList = userService.searchUsers(search).stream()
                    .filter(u -> u.getRole() == Role.WARGA)
                    .toList();
        } else {
            wargaList = userService.getUsersByRole(Role.WARGA);
        }
        model.addAttribute("wargaList", wargaList);
        model.addAttribute("search", search);
        return "petugas/warga";
    }

    @GetMapping("/setoran")
    public String tambahSetoran(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<KategoriSampah> kategoriList = kategoriSampahRepository.findAll();
        List<User> wargaList = userService.getUsersByRole(Role.WARGA);
        model.addAttribute("kategoriList", kategoriList);
        model.addAttribute("wargaList", wargaList);
        return "petugas/setoran";
    }

    @PostMapping("/setoran/simpan")
    public String simpanSetoran(@RequestParam Long wargaId,
                                @RequestParam("kategoriIds") List<Long> kategoriIds,
                                @RequestParam("beratKgs") List<BigDecimal> beratKgs,
                                @RequestParam(required = false) String catatan,
                                Authentication authentication,
                                RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/setoran";
            }

            if (kategoriIds == null || kategoriIds.isEmpty()) {
                redirectAttrs.addFlashAttribute("error", "Pilih minimal satu kategori sampah");
                return "redirect:/petugas/setoran";
            }

            for (int i = 0; i < kategoriIds.size(); i++) {
                Setoran setoran = setoranService.createSetoranSampah(
                        wargaId, kategoriIds.get(i), beratKgs.get(i), catatan
                );
                setoranService.verifikasiSetoran(setoran.getId(), petugas.getId(), StatusSetoran.DITERIMA, catatan);
            }

            redirectAttrs.addFlashAttribute("success", "Setoran berhasil disimpan dan diverifikasi");
            return "redirect:/petugas/setoran";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menyimpan setoran: " + e.getMessage());
            return "redirect:/petugas/setoran";
        }
    }

    @GetMapping("/verifikasi")
    public String verifikasiSetoran(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<Setoran> allMenunggu = setoranService.getSetoranByStatusPembayaran(StatusPembayaran.MENUNGGU_VERIFIKASI);
        List<Setoran> setoranBiasa = allMenunggu.stream()
                .filter(s -> s.getKategori() == null || !Boolean.TRUE.equals(s.getKategori().getIsDaurUlang()))
                .toList();
        model.addAttribute("setoranList", setoranBiasa);

        List<Setoran> allSetoran = setoranService.getAllSetoranSampah();
        List<Setoran> daurUlangMenunggu = allSetoran.stream()
                .filter(s -> s.getKategori() != null
                        && Boolean.TRUE.equals(s.getKategori().getIsDaurUlang())
                        && s.getStatus() == StatusSetoran.MENUNGGU)
                .toList();
        model.addAttribute("daurUlangList", daurUlangMenunggu);

        model.addAttribute("penjemputanList", setoranService.getSetoranByStatus(StatusSetoran.DITERIMA));
        return "petugas/verifikasi";
    }

    @PostMapping("/verifikasi/pembayaran/{id}")
    public String prosesVerifikasiPembayaran(@PathVariable Long id,
                                              @RequestParam StatusPembayaran statusPembayaran,
                                              @RequestParam(required = false) String catatan,
                                              Authentication authentication,
                                              RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/verifikasi";
            }
            setoranService.verifikasiPembayaran(id, petugas.getId(), statusPembayaran, catatan);
            String msg = statusPembayaran == StatusPembayaran.DISETUJUI
                    ? "Pembayaran disetujui, penjemputan dijadwalkan"
                    : "Pembayaran ditolak";
            redirectAttrs.addFlashAttribute("success", msg);
            return "redirect:/petugas/verifikasi";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal memverifikasi pembayaran: " + e.getMessage());
            return "redirect:/petugas/verifikasi";
        }
    }

    @PostMapping("/verifikasi/penjemputan/{id}")
    public String prosesUpdatePenjemputan(@PathVariable Long id,
                                           @RequestParam StatusPenjemputan statusPenjemputan,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/verifikasi";
            }
            setoranService.updateStatusPenjemputan(id, statusPenjemputan);
            String msg = statusPenjemputan == StatusPenjemputan.SEDANG_DIJEMPUT
                    ? "Status penjemputan diubah menjadi Sedang Dijemput"
                    : "Status penjemputan diubah menjadi Selesai";
            redirectAttrs.addFlashAttribute("success", msg);
            return "redirect:/petugas/verifikasi";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengupdate status penjemputan: " + e.getMessage());
            return "redirect:/petugas/verifikasi";
        }
    }

    @PostMapping("/verifikasi/daur-ulang/{id}")
    public String terimaDaurUlang(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/verifikasi";
            }
            setoranService.terimaSetoranDaurUlang(id, petugas.getId());
            redirectAttrs.addFlashAttribute("success", "Setoran daur ulang diterima, saldo warga telah dikredit");
            return "redirect:/petugas/verifikasi";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal memverifikasi setoran daur ulang: " + e.getMessage());
            return "redirect:/petugas/verifikasi";
        }
    }

    @GetMapping("/penarikan")
    public String listPenarikan(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        model.addAttribute("penarikanList", bankSampahService.getPenarikanByStatus(StatusPenarikan.MENUNGGU));
        model.addAttribute("riwayatPenarikan", bankSampahService.getPenarikanByStatus(StatusPenarikan.DISETUJUI));
        model.addAttribute("riwayatPenarikanDitolak", bankSampahService.getPenarikanByStatus(StatusPenarikan.DITOLAK));
        return "petugas/penarikan";
    }

    @PostMapping("/penarikan/{id}/setujui")
    public String setujuiPenarikan(@PathVariable Long id,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/penarikan";
            }
            bankSampahService.setujuiPenarikan(id, petugas.getId());
            redirectAttrs.addFlashAttribute("success", "Penarikan saldo disetujui");
            return "redirect:/petugas/penarikan";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menyetujui penarikan: " + e.getMessage());
            return "redirect:/petugas/penarikan";
        }
    }

    @PostMapping("/penarikan/{id}/tolak")
    public String tolakPenarikan(@PathVariable Long id,
                                 @RequestParam(required = false) String catatan,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/penarikan";
            }
            bankSampahService.tolakPenarikan(id, petugas.getId(), catatan);
            redirectAttrs.addFlashAttribute("success", "Penarikan saldo ditolak");
            return "redirect:/petugas/penarikan";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menolak penarikan: " + e.getMessage());
            return "redirect:/petugas/penarikan";
        }
    }

    @PostMapping("/verifikasi/{id}")
    public String prosesVerifikasi(@PathVariable Long id,
                                   @RequestParam StatusSetoran status,
                                   @RequestParam(required = false) String catatan,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttrs) {
        try {
            User petugas = getCurrentUser(authentication);
            if (petugas == null) {
                redirectAttrs.addFlashAttribute("error", "Petugas tidak ditemukan");
                return "redirect:/petugas/verifikasi";
            }
            setoranService.verifikasiSetoran(id, petugas.getId(), status, catatan);
            redirectAttrs.addFlashAttribute("success", "Setoran berhasil " + status.name().toLowerCase());
            return "redirect:/petugas/verifikasi";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal memverifikasi setoran: " + e.getMessage());
            return "redirect:/petugas/verifikasi";
        }
    }
}
