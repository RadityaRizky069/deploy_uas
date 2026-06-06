package com.example.trashformer.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "landing";
        }
        var authorities = authentication.getAuthorities();
        if (authorities.isEmpty()) {
            return "redirect:/login";
        }
        String role = authorities.iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (role.equals("ROLE_PETUGAS")) {
            return "redirect:/petugas/dashboard";
        } else if (role.equals("ROLE_WARGA")) {
            return "redirect:/warga/dashboard";
        } else {
            return "redirect:/login";
        }
    }
}
