package com.example.trashformer.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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
import com.example.trashformer.model.StatusTopup;
import com.example.trashformer.model.TopupSaldo;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.KategoriSampahRepository;
import com.example.trashformer.service.BankSampahService;
import com.example.trashformer.service.SetoranService;
import com.example.trashformer.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final SetoranService setoranService;
    private final KategoriSampahRepository kategoriSampahRepository;
    private final BankSampahService bankSampahService;

    public AdminController(UserService userService,
                           SetoranService setoranService,
                           KategoriSampahRepository kategoriSampahRepository,
                           BankSampahService bankSampahService) {
        this.userService = userService;
        this.setoranService = setoranService;
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

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        Map<String, Long> stats = setoranService.getDashboardStats();
        model.addAttribute("totalWarga", stats.getOrDefault("totalWarga", 0L));
        model.addAttribute("totalPetugas", stats.getOrDefault("totalPetugas", 0L));
        model.addAttribute("setoranHariIni", stats.getOrDefault("totalSetoranHariIni", 0L));
        model.addAttribute("totalSampah", stats.getOrDefault("totalBerat", 0L));

        List<Setoran> allSetoran = setoranService.getAllSetoranSampah();
        List<Setoran> recent = allSetoran.size() > 5 ? allSetoran.subList(0, 5) : allSetoran;
        model.addAttribute("recentSetoran", recent);

        List<Object[]> kategoriData = setoranService.getBeratPerKategori();
        List<String> kategoriLabels = new ArrayList<>();
        List<Double> kategoriValues = new ArrayList<>();
        for (Object[] row : kategoriData) {
            kategoriLabels.add((String) row[0]);
            kategoriValues.add(((Number) row[1]).doubleValue());
        }
        model.addAttribute("kategoriLabels", kategoriLabels);
        model.addAttribute("kategoriValues", kategoriValues);

        List<String> warnaKategori = Arrays.asList(
            "#2ecc71", "#3498db", "#e74c3c", "#f39c12", "#9b59b6",
            "#1abc9c", "#e67e22", "#34495e", "#16a085", "#c0392b"
        );
        model.addAttribute("warnaKategori", warnaKategori);

        int currentYear = LocalDate.now().getYear();
        List<Object[]> bulanData = setoranService.getBeratPerBulan(currentYear);
        double[] bulanValues = new double[12];
        for (Object[] row : bulanData) {
            int month = ((Number) row[0]).intValue();
            double berat = ((Number) row[1]).doubleValue();
            if (month >= 1 && month <= 12) {
                bulanValues[month - 1] = berat;
            }
        }
        model.addAttribute("bulanData", bulanValues);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String search,
                            Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
        } else {
            users = userService.getAllUsers();
        }
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/users";
    }

    @GetMapping("/users/tambah")
    public String tambahUser(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }

    @PostMapping("/users/simpan")
    public String simpanUser(@RequestParam String nama,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttrs) {
        String namaTrim = nama.trim();
        String usernameTrim = username.trim().toLowerCase();
        String passwordTrim = password.trim();

        if (namaTrim.isEmpty() || usernameTrim.isEmpty() || passwordTrim.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Semua field harus diisi");
            return "redirect:/admin/users/tambah";
        }

        if (passwordTrim.length() < 6) {
            redirectAttrs.addFlashAttribute("error", "Password minimal 6 karakter");
            return "redirect:/admin/users/tambah";
        }

        if (userService.getUserByUsername(usernameTrim).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Username sudah digunakan");
            return "redirect:/admin/users/tambah";
        }

        try {
            userService.createUser(namaTrim, usernameTrim, passwordTrim, role);
            redirectAttrs.addFlashAttribute("success", "User berhasil ditambahkan");
            return "redirect:/admin/users";
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("error", "Username sudah digunakan");
            return "redirect:/admin/users/tambah";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Terjadi kesalahan sistem");
            return "redirect:/admin/users/tambah";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }

    @PostMapping("/users/update")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String nama,
                             @RequestParam String username,
                             @RequestParam Role role,
                             @RequestParam(required = false) Boolean isActive,
                             RedirectAttributes redirectAttrs) {
        try {
            userService.updateUser(id, nama, username, role, isActive);
            redirectAttrs.addFlashAttribute("success", "User berhasil diupdate");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengupdate user");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/hapus/{id}")
    public String hapusUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteUser(id);
            redirectAttrs.addFlashAttribute("success", "User berhasil dihapus");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menghapus user");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.resetPassword(id, "123456");
            redirectAttrs.addFlashAttribute("success", "Password berhasil direset ke 123456");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mereset password");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/toggle/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.toggleActive(id);
            redirectAttrs.addFlashAttribute("success", "Status user berhasil diubah");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal mengubah status user");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/setoran")
    public String listSetoran(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        model.addAttribute("setoranList", setoranService.getAllSetoranSampah());
        return "admin/setoran";
    }

    @GetMapping("/setoran-uang")
    public String listSetoranUang(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        model.addAttribute("setoranUangList", setoranService.getAllSetoranUang());
        return "admin/setoran-uang";
    }

    @GetMapping("/kategori")
    public String listKategori(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<KategoriSampah> kategoriList = kategoriSampahRepository.findAll();
        model.addAttribute("listKategori", kategoriList);
        return "admin/kategori";
    }

    @PostMapping("/kategori/simpan")
    public String simpanKategori(@RequestParam String namaKategori,
                                  @RequestParam(required = false) BigDecimal hargaPerKg,
                                  @RequestParam(defaultValue = "false") boolean isDaurUlang,
                                  @RequestParam(required = false) BigDecimal hargaDaurUlang,
                                  RedirectAttributes redirectAttrs) {
        String namaTrim = namaKategori.trim();
        if (namaTrim.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Nama kategori harus diisi");
            return "redirect:/admin/kategori";
        }

        try {
            KategoriSampah kategori = new KategoriSampah();
            kategori.setNama(namaTrim);
            if (hargaPerKg != null) {
                kategori.setHargaPerKg(hargaPerKg);
            }
            kategori.setIsDaurUlang(isDaurUlang);
            if (isDaurUlang && hargaDaurUlang != null) {
                kategori.setHargaDaurUlang(hargaDaurUlang);
            }
            kategoriSampahRepository.save(kategori);
            redirectAttrs.addFlashAttribute("success", "Kategori berhasil ditambahkan");
            return "redirect:/admin/kategori?suksesTambah";
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("error", "Kategori dengan nama tersebut sudah ada");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menambahkan kategori");
        }
        return "redirect:/admin/kategori";
    }

    @GetMapping("/kategori/edit/{id}")
    public String editKategori(@PathVariable Long id, Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        KategoriSampah kategori = kategoriSampahRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
        model.addAttribute("kategori", kategori);
        return "admin/kategori-form";
    }

    @PostMapping("/kategori/update")
    public String updateKategori(@RequestParam Long id,
                                  @RequestParam String namaKategori,
                                  @RequestParam(required = false) BigDecimal hargaPerKg,
                                  @RequestParam(defaultValue = "false") boolean isDaurUlang,
                                  @RequestParam(required = false) BigDecimal hargaDaurUlang,
                                  RedirectAttributes redirectAttrs) {
        String namaTrim = namaKategori.trim();
        if (namaTrim.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Nama kategori harus diisi");
            return "redirect:/admin/kategori/edit/" + id;
        }

        try {
            KategoriSampah kategori = kategoriSampahRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
            kategori.setNama(namaTrim);
            kategori.setHargaPerKg(hargaPerKg);
            kategori.setIsDaurUlang(isDaurUlang);
            if (isDaurUlang && hargaDaurUlang != null) {
                kategori.setHargaDaurUlang(hargaDaurUlang);
            } else {
                kategori.setHargaDaurUlang(null);
            }
            kategoriSampahRepository.save(kategori);
            redirectAttrs.addFlashAttribute("success", "Kategori berhasil diperbarui");
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("error", "Kategori dengan nama tersebut sudah ada");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal memperbarui kategori");
        }
        return "redirect:/admin/kategori";
    }

    @GetMapping("/kategori/hapus/{id}")
    public String hapusKategori(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            kategoriSampahRepository.deleteById(id);
            redirectAttrs.addFlashAttribute("success", "Kategori berhasil dihapus");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menghapus kategori");
        }
        return "redirect:/admin/kategori";
    }

    @GetMapping("/topup")
    public String topupList(Authentication authentication, Model model) {
        addUserToModel(authentication, model);
        List<TopupSaldo> menunggu = bankSampahService.getTopupByStatus(StatusTopup.MENUNGGU);
        List<TopupSaldo> riwayatDisetujui = bankSampahService.getTopupByStatus(StatusTopup.DISETUJUI);
        List<TopupSaldo> riwayatDitolak = bankSampahService.getTopupByStatus(StatusTopup.DITOLAK);
        model.addAttribute("topupList", menunggu);
        model.addAttribute("riwayatDisetujui", riwayatDisetujui);
        model.addAttribute("riwayatDitolak", riwayatDitolak);
        return "admin/topup";
    }

    @PostMapping("/topup/setujui/{id}")
    public String setujuiTopup(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttrs) {
        try {
            User admin = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan"));
            bankSampahService.setujuiTopup(id, admin.getId());
            redirectAttrs.addFlashAttribute("success", "Topup disetujui, saldo warga bertambah");
            return "redirect:/admin/topup";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menyetujui topup: " + e.getMessage());
            return "redirect:/admin/topup";
        }
    }

    @PostMapping("/topup/tolak/{id}")
    public String tolakTopup(@PathVariable Long id,
                             @RequestParam(required = false) String catatan,
                             Authentication authentication,
                             RedirectAttributes redirectAttrs) {
        try {
            User admin = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan"));
            bankSampahService.tolakTopup(id, admin.getId(), catatan);
            redirectAttrs.addFlashAttribute("success", "Topup ditolak");
            return "redirect:/admin/topup";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gagal menolak topup: " + e.getMessage());
            return "redirect:/admin/topup";
        }
    }
}
