package com.essencedetoi.controller;

import com.essencedetoi.dto.UserRegistrationDto;
// import com.essencedetoi.model.User; // Removed as unused
import com.essencedetoi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        }
        return "login"; // Corresponde a src/main/resources/templates/login.html
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("errorMessage", "Error durante el inicio de sesión. Inténtalo de nuevo.");
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register"; // Corresponde a src/main/resources/templates/register.html
    }

    @PostMapping("/register/save")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (userService.existsByUsername(registrationDto.getUsername())) {
            result.rejectValue("username", "user.exists", "Este nombre de usuario ya está en uso.");
        }

        if (userService.existsByEmail(registrationDto.getEmail())) {
            result.rejectValue("email", "email.exists", "Este correo electrónico ya está registrado.");
        }

        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null &&
            !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden.");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", registrationDto);
            return "register";
        }

        try {
            userService.save(registrationDto); // Guarda el nuevo usuario como CLIENTE
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", registrationDto);
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("user", registrationDto);
            model.addAttribute("errorMessage", "Ocurrió un error durante el registro. Por favor, inténtalo de nuevo.");
            return "register";
        }
    }
}
