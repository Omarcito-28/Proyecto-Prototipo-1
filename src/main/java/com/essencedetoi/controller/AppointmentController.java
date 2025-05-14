package com.essencedetoi.controller;

import com.essencedetoi.dto.AppointmentRequestDto;
import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.User;
import com.essencedetoi.model.Service; // Entidad Service
import com.essencedetoi.service.AppointmentService;
import com.essencedetoi.service.SalonServiceService;
import com.essencedetoi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
@PreAuthorize("isAuthenticated()") // Todas las rutas requieren autenticación
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final SalonServiceService salonServiceService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, UserService userService, SalonServiceService salonServiceService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.salonServiceService = salonServiceService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos."));
    }

    // Listar todas las citas (quizás más para admin o un dashboard general)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        model.addAttribute("appointments", appointments);
        return "admin/all_appointments"; //  Vista para administradores
    }

    // Listar citas para el usuario actual (cliente o estilista)
    @GetMapping("/my")
    public String listMyAppointments(Model model) {
        User currentUser = getCurrentUser();
        List<Appointment> appointments;
        boolean isStylist = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STYLIST"));

        if (isStylist) {
            appointments = appointmentService.getAppointmentsForStylist(currentUser.getId());
            model.addAttribute("viewType", "stylist");
        } else { // Es cliente
            appointments = appointmentService.getAppointmentsForClient(currentUser.getId());
            model.addAttribute("viewType", "client");
        }
        model.addAttribute("appointments", appointments);
        return "appointment/my_appointments"; // src/main/resources/templates/appointment/my_appointments.html
    }

    // Mostrar formulario para nueva cita
    @GetMapping("/new")
    public String showNewAppointmentForm(Model model) {
        model.addAttribute("appointmentRequestDto", new AppointmentRequestDto());
        model.addAttribute("stylists", userService.findAllStylists());
        model.addAttribute("services", salonServiceService.getAllServices());
        return "appointment/appointment_form"; // src/main/resources/templates/appointment/appointment_form.html
    }

    @PostMapping("/save")
    public String saveAppointment(@Valid @ModelAttribute("appointmentRequestDto") AppointmentRequestDto appointmentDto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        // Log al inicio del método
        System.out.println("LOG: Ingresando a saveAppointment. DTO recibido: " + appointmentDto.toString());

        User currentUser = getCurrentUser();
        appointmentDto.setClientId(currentUser.getId()); // Asignar el cliente actual

        Optional<Service> serviceOpt = salonServiceService.getServiceById(appointmentDto.getServiceId());
        if (serviceOpt.isEmpty()) {
            result.rejectValue("serviceId", "notfound", "Servicio no encontrado.");
            System.out.println("LOG: Error de validación - Servicio no encontrado. ID: " + appointmentDto.getServiceId());
        }

        if (appointmentDto.getAppointmentDateTime() != null && appointmentDto.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
             result.rejectValue("appointmentDateTime", "past.date", "La fecha de la cita no puede ser en el pasado.");
             System.out.println("LOG: Error de validación - La fecha de la cita está en el pasado: " + appointmentDto.getAppointmentDateTime());
        }

        if (serviceOpt.isPresent() && appointmentDto.getAppointmentDateTime() != null) {
            boolean isAvailable = appointmentService.isStylistAvailable(
                appointmentDto.getStylistId(), 
                appointmentDto.getAppointmentDateTime(), 
                serviceOpt.get().getDurationMinutes(), 
                null // existingAppointmentId es null para nuevas citas
            );
            if (!isAvailable) {
                result.rejectValue("appointmentDateTime", "stylist.unavailable", "El estilista no está disponible en la fecha y hora seleccionada para este servicio.");
                System.out.println("LOG: Error de validación - Estilista no disponible. StylistID: " + appointmentDto.getStylistId() + ", DateTime: " + appointmentDto.getAppointmentDateTime());
            } else {
                System.out.println("LOG: Verificación de disponibilidad - Estilista SÍ disponible.");
            }
        } else {
             System.out.println("LOG: No se pudo verificar disponibilidad del estilista porque serviceOpt no está presente ("+serviceOpt.isPresent()+") o appointmentDateTime es null ("+(appointmentDto.getAppointmentDateTime() == null)+").");
        }

        // Log antes de revisar result.hasErrors()
        System.out.println("LOG: Revisando errores de BindingResult. result.hasErrors(): " + result.hasErrors());
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> 
                System.out.println("LOG: Error de validación en BindingResult: Field '" + 
                                   (error instanceof org.springframework.validation.FieldError ? ((org.springframework.validation.FieldError)error).getField() : "global") + 
                                   "', Message: '" + error.getDefaultMessage() + 
                                   "', Code: '" + error.getCode() + "'")
            );
            model.addAttribute("appointmentRequestDto", appointmentDto);
            model.addAttribute("stylists", userService.findAllStylists());
            model.addAttribute("services", salonServiceService.getAllServices());
            System.out.println("LOG: Devolviendo appointment_form debido a errores de validación.");
            return "appointment/appointment_form";
        }

        try {
            System.out.println("LOG: Intentando crear la cita...");
            appointmentService.createAppointment(appointmentDto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cita agendada exitosamente.");
            System.out.println("LOG: Cita creada exitosamente. Redirigiendo a /appointments/my");
            return "redirect:/appointments/my";
        } catch (IllegalArgumentException e) {
            // Log dentro del catch
            System.err.println("LOG: Excepción IllegalArgumentException en createAppointment: " + e.getMessage());
            model.addAttribute("appointmentRequestDto", appointmentDto);
            model.addAttribute("stylists", userService.findAllStylists());
            model.addAttribute("services", salonServiceService.getAllServices());
            model.addAttribute("errorMessage", e.getMessage());
            System.out.println("LOG: Devolviendo appointment_form debido a IllegalArgumentException.");
            return "appointment/appointment_form";
        } catch (Exception e) { // Un catch más general para cualquier otra cosa
            System.err.println("LOG: Excepción INESPERADA en saveAppointment: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo para esta
            model.addAttribute("appointmentRequestDto", appointmentDto);
            model.addAttribute("stylists", userService.findAllStylists());
            model.addAttribute("services", salonServiceService.getAllServices());
            model.addAttribute("errorMessage", "Ocurrió un error inesperado al procesar su solicitud.");
            System.out.println("LOG: Devolviendo appointment_form debido a una excepción inesperada.");
            return "appointment/appointment_form";
        }
    }

    // Métodos para cancelar, completar citas (se añadirán después o en un controlador más específico si es necesario)
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable("id") Long appointmentId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        try {
            appointmentService.cancelAppointment(appointmentId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cita cancelada exitosamente.");
        } catch (AccessDeniedException ade) {
             redirectAttributes.addFlashAttribute("errorMessage", "No tiene permisos para cancelar esta cita.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/appointments/my";
    }

    @PostMapping("/complete/{id}")
    @PreAuthorize("hasAnyRole('STYLIST', 'ADMIN')")
    public String completeAppointment(@PathVariable("id") Long appointmentId, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        try {
            appointmentService.completeAppointment(appointmentId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cita marcada como completada.");
        } catch (AccessDeniedException ade) {
             redirectAttributes.addFlashAttribute("errorMessage", "No tiene permisos para completar esta cita.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/appointments/my"; // O al dashboard del estilista
    }

    // GET para ver detalles de una cita (podría ser útil)
    @GetMapping("/{id}")
    public String viewAppointmentDetails(@PathVariable("id") Long appointmentId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Appointment> appointmentOptional = appointmentService.getAppointmentById(appointmentId);
        User currentUser = getCurrentUser();

        if (appointmentOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cita no encontrada.");
            return "redirect:/appointments/my";
        }
        Appointment appointment = appointmentOptional.get();
        
        // Verificar permisos para ver detalles
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isClientOwner = appointment.getClient().getId().equals(currentUser.getId());
        boolean isStylistAssigned = appointment.getStylist().getId().equals(currentUser.getId());

        if (!isAdmin && !isClientOwner && !isStylistAssigned) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tiene permisos para ver esta cita.");
            return "redirect:/appointments/my"; 
        }

        model.addAttribute("appointment", appointment);
        return "appointment/appointment_details"; // src/main/resources/templates/appointment/appointment_details.html
    }

}
