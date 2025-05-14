package com.essencedetoi.service;

import com.essencedetoi.dto.UserRegistrationDto;
import com.essencedetoi.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService { // Extiende UserDetailsService para la integración con Spring Security

    User save(UserRegistrationDto registrationDto);
    User registerNewUser(UserRegistrationDto registrationDto, String roleName);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    default Optional<User> getUserById(Long id) {
        return findById(id);
    }
    List<User> findAllUsers();
    List<User> findAllClients();
    List<User> findAllStylists();
    User updateUser(Long id, User userDetails); // Podríamos usar un DTO aquí también
    void deleteUser(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void updateUserRolesAndStatus(Long userId, List<Long> roleIds, boolean enabled);
    long countAllUsers();
    User updateUserProfile(Long userId, String fullName, String email, String phoneNumber, String specialties);
}
