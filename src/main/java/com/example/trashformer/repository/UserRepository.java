package com.example.trashformer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trashformer.model.Role;
import com.example.trashformer.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    List<User> findByNamaContainingIgnoreCaseOrUsernameContainingIgnoreCase(String nama, String username);

    long countByRole(Role role);
}
