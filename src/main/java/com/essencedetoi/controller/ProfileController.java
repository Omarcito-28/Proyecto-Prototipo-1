package com.essencedetoi.controller;

import com.essencedetoi.model.User;
import com.essencedetoi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal User currentUser, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication: {}", auth);
        logger.info("Is authenticated: {}", auth != null && auth.isAuthenticated());
        logger.info("Current user from @AuthenticationPrincipal: {}", currentUser);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        // Obtener usuario actualizado de la base de datos
        User user = userService.getUserById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        model.addAttribute("user", user);
        return "profile/edit_profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal User currentUser,
            Authentication authentication,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String specialties,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Update Profile - Authentication: {}", authentication);
        logger.info("Update Profile - Current user: {}", currentUser);
        
        if (currentUser == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Update Profile - SecurityContext Authentication: {}", auth);
            redirectAttributes.addFlashAttribute("error", "Debe iniciar sesión para actualizar el perfil");
            return "redirect:/login";
        }
        
        try {
            userService.updateUserProfile(
                currentUser.getId(),
                fullName,
                email,
                phoneNumber,
                specialties
            );
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
