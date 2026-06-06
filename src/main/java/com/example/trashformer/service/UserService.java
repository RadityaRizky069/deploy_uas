package com.example.trashformer.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.trashformer.model.Role;
import com.example.trashformer.model.User;
import com.example.trashformer.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerWarga(String nama, String username, String password) {
        User user = new User();
        user.setNama(nama.trim());
        user.setUsername(username.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setRole(Role.WARGA);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User createUser(String nama, String username, String password, Role role) {
        User user = new User();
        user.setNama(nama.trim());
        user.setUsername(username.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    public User updateUser(Long id, String nama, String username, Role role, Boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        if (nama != null && !nama.trim().isEmpty()) {
            user.setNama(nama.trim());
        }
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim().toLowerCase());
        }
        if (role != null) {
            user.setRole(role);
        }
        if (isActive != null) {
            user.setActive(isActive);
        }
        return userRepository.save(user);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        boolean currentStatus = user.isActive() == null || user.isActive();
        user.setActive(!currentStatus);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByNamaContainingIgnoreCaseOrUsernameContainingIgnoreCase(keyword.trim(), keyword.trim());
    }
}
