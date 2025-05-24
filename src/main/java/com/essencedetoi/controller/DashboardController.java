package com.essencedetoi.controller;

import com.essencedetoi.model.User;
import com.essencedetoi.model.Appointment;
import com.essencedetoi.service.AppointmentService;
import com.essencedetoi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    @Autowired
    public DashboardController(UserService userService, AppointmentService appointmentService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(currentUsername);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("currentUser", user);

            // Determinar el rol del usuario y redirigir al dashboard correspondiente
            if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                // Cargar datos específicos para administradores
                model.addAttribute("totalUsers", userService.countAllUsers());
                // Aquí puedes agregar más datos para el dashboard de admin
                return "admin/admin_dashboard";
            } else if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STYLIST"))) {
                // Cargar datos específicos para estilistas
                List<Appointment> appointments = appointmentService.getAppointmentsForStylist(user.getId());
                long completedAppointments = appointmentService.countCompletedAppointmentsForStylist(user.getId());
                model.addAttribute("appointments", appointments);
                model.addAttribute("completedAppointmentsCount", completedAppointments);
                return "stylist/stylist_dashboard";
            } else {
                // Por defecto, mostrar dashboard de cliente
                model.addAttribute("upcomingAppointments", appointmentService.getUpcomingAppointmentsForClient(user.getId()));
                return "client/client_dashboard";
            }
        } else {
            // Manejar caso donde el usuario autenticado no se encuentra en la BD
            return "redirect:/login?error=UserNotFound";
        }
    }
}
