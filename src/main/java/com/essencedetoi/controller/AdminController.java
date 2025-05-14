package com.essencedetoi.controller;

import com.essencedetoi.dto.UserRoleUpdateDto;
import com.essencedetoi.model.Role;
import com.essencedetoi.model.User;
import com.essencedetoi.service.RoleService;
import com.essencedetoi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin") // Todas las rutas en este controlador comenzarán con /admin
@PreAuthorize("hasRole('ADMIN')") // Requiere rol ADMIN para todas las rutas aquí
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Podríamos cargar datos específicos para el dashboard del admin
        long totalUsers = userService.findAllUsers().size(); // Método findAllUsers() debe existir en UserService
        model.addAttribute("totalUsers", totalUsers);
        // model.addAttribute("activePage", "adminDashboard");
        return "admin/admin_dashboard"; // src/main/resources/templates/admin/admin_dashboard.html
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers(); // Método findAllUsers() debe existir en UserService
        model.addAttribute("users", users);
        // model.addAttribute("activePage", "userManagement");
        return "admin/admin_user_list"; // Corregido: src/main/resources/templates/admin/admin_user_list.html
    }

    @GetMapping("/users/edit/{userId}")
    public String showEditUserRolesForm(@PathVariable("userId") Long userId, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findById(userId); // Método findById debe existir en UserService
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado con ID: " + userId);
            return "redirect:/admin/users";
        }

        User user = userOptional.get();
        List<Role> allRoles = roleService.findAllRoles(); // Método findAllRoles debe existir en RoleService
        List<Long> assignedRoleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());

        UserRoleUpdateDto userRoleUpdateDto = new UserRoleUpdateDto();
        userRoleUpdateDto.setRoleIds(assignedRoleIds);
        userRoleUpdateDto.setEnabled(user.isEnabled());

        model.addAttribute("user", user);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("assignedRoleIds", assignedRoleIds);
        model.addAttribute("userRoleUpdateDto", userRoleUpdateDto);
        // model.addAttribute("activePage", "userManagement");

        return "admin/admin_user_edit_form"; // src/main/resources/templates/admin/admin_user_edit_form.html
    }

    @PostMapping("/users/update-roles/{userId}")
    public String updateUserRolesAndStatus(@PathVariable("userId") Long userId,
                                  @ModelAttribute("userRoleUpdateDto") UserRoleUpdateDto userRoleUpdateDto,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Este método necesitará ser implementado en UserService
            userService.updateUserRolesAndStatus(userId, userRoleUpdateDto.getRoleIds(), userRoleUpdateDto.isEnabled());
            redirectAttributes.addFlashAttribute("successMessage", "Roles y estado del usuario actualizados correctamente.");
        } catch (Exception e) {
            // Considerar loggear el error e.getMessage() o e.printStackTrace()
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
            return "redirect:/admin/users/edit/" + userId; // Volver al form de edición en caso de error
        }
        return "redirect:/admin/users";
    }

    // Método para eliminar usuario (ya existente, se mantiene)
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId); // Método deleteUser debe existir en UserService
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    // Endpoints para la gestión de servicios del salón (nombre, descripción, precio, duración)
    // se añadirán en un ServiceController o similar, pero podrían estar aquí si son solo admin.
}
