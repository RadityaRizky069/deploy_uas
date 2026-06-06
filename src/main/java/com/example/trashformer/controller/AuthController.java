package com.example.trashformer.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.trashformer.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register/save")
    public String register(
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttrs) {

        String namaTrim = (nama != null) ? nama.trim() : "";
        String usernameTrim = (username != null) ? username.trim().toLowerCase() : "";
        String passwordTrim = (password != null) ? password.trim() : "";

        if (namaTrim.isEmpty() || usernameTrim.isEmpty() || passwordTrim.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Semua field harus diisi");
            return "redirect:/register?error";
        }

        if (passwordTrim.length() < 8) {
            redirectAttrs.addFlashAttribute("error", "Password minimal 8 karakter");
            return "redirect:/register?error";
        }

        if (!passwordTrim.matches(".*[A-Z].*")) {
            redirectAttrs.addFlashAttribute("error", "Password harus mengandung huruf besar");
            return "redirect:/register?error";
        }

        if (!passwordTrim.matches(".*[0-9].*")) {
            redirectAttrs.addFlashAttribute("error", "Password harus mengandung angka");
            return "redirect:/register?error";
        }

        if (usernameTrim.length() < 3) {
            redirectAttrs.addFlashAttribute("error", "Username minimal 3 karakter");
            return "redirect:/register?error";
        }

        if (userService.getUserByUsername(usernameTrim).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Username sudah digunakan");
            return "redirect:/register?error";
        }

        try {
            userService.registerWarga(namaTrim, usernameTrim, passwordTrim);
            return "redirect:/login?register_success";

        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("error", "Username sudah digunakan");
            return "redirect:/register?error";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Terjadi kesalahan sistem. Silakan coba lagi.");
            return "redirect:/register?error";
        }
    }
}
