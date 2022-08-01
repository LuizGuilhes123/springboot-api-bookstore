package com.fredfonseca.bookstoremanager.users.repository;

import com.fredfonseca.bookstoremanager.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailOrUsername(String email, String username);
}
