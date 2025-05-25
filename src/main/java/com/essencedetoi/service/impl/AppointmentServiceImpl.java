package com.essencedetoi.service.impl;

import com.essencedetoi.dto.AppointmentRequestDto;
import com.essencedetoi.model.*;
import com.essencedetoi.repository.AppointmentRepository;
import com.essencedetoi.repository.ServiceRepository;
import com.essencedetoi.repository.UserRepository;
import com.essencedetoi.service.AppointmentService;
import com.essencedetoi.validation.AppointmentDateTimeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository salonServiceRepository;
    private final AppointmentDateTimeValidator appointmentDateTimeValidator;

    @Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                UserRepository userRepository,
                                ServiceRepository salonServiceRepository,
                                AppointmentDateTimeValidator appointmentDateTimeValidator) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.appointmentDateTimeValidator = appointmentDateTimeValidator;
    }

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentRequestDto appointmentDto, User authenticatedClient) {
        User client = userRepository.findById(authenticatedClient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + authenticatedClient.getId()));
        User stylist = userRepository.findById(appointmentDto.getStylistId())
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado con ID: " + appointmentDto.getStylistId()));
        com.essencedetoi.model.Service service = salonServiceRepository.findById(appointmentDto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + appointmentDto.getServiceId()));

        if (!isStylistAvailable(stylist.getId(), appointmentDto.getAppointmentDateTime(), service.getDurationMinutes(), null)) {
            throw new IllegalArgumentException("El estilista no está disponible en la fecha y hora seleccionadas.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setStylist(stylist);
        appointment.setService(service);
        appointment.setAppointmentDateTime(appointmentDto.getAppointmentDateTime());
        appointment.setNotes(appointmentDto.getNotes());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());

        // Validar la fecha antes de guardar
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(appointment, "appointment");
        appointmentDateTimeValidator.validate(appointment, errors);
        if (errors.hasErrors()) {
            String errorMessage = errors.getFieldError() != null ? 
                errors.getFieldError().getDefaultMessage() : 
                "Error de validación en la fecha de la cita";            
            throw new IllegalArgumentException(errorMessage);
        }

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForClient(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForStylist(Long stylistId) {
        return appointmentRepository.findByStylistId(stylistId)
                .stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.SCHEDULED)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countCompletedAppointmentsForStylist(Long stylistId) {
        return appointmentRepository.findByStylistId(stylistId)
                .stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.COMPLETED)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForStylistBetweenDates(Long stylistId, LocalDateTime start, LocalDateTime end) {
        User stylist = userRepository.findById(stylistId).orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado"));
        return appointmentRepository.findByStylistAndAppointmentDateTimeBetween(stylist, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForClientBetweenDates(Long clientId, LocalDateTime start, LocalDateTime end) {
        User client = userRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return appointmentRepository.findByClientAndAppointmentDateTimeBetween(client, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointmentsForStylist(Long stylistId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return getAppointmentsForStylistBetweenDates(stylistId, startOfDay, endOfDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsForClient(Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthFromNow = now.plusMonths(1);
        return getAppointmentsForClientBetweenDates(clientId, now, oneMonthFromNow)
                .stream()
                .filter(appointment -> appointment.getStatus() == AppointmentStatus.SCHEDULED)
                .toList();
    }

    @Override
    @Transactional
    public Appointment updateAppointment(Long id, AppointmentRequestDto appointmentDto, User requestingUser) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        // Verificar permisos: Solo el cliente que creó la cita o un admin pueden modificarla.
        boolean isAdmin = requestingUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!existingAppointment.getClient().getId().equals(requestingUser.getId()) && !isAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar esta cita.");
        }
        // Clientes no pueden cambiar el estilista o servicio tan fácilmente, o la fecha a una ya pasada.
        // Admin podría tener más permisos.

        User stylist = userRepository.findById(appointmentDto.getStylistId())
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado con ID: " + appointmentDto.getStylistId()));
        com.essencedetoi.model.Service service = salonServiceRepository.findById(appointmentDto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + appointmentDto.getServiceId()));

        if (!isStylistAvailable(stylist.getId(), appointmentDto.getAppointmentDateTime(), service.getDurationMinutes(), existingAppointment.getId())) {
            throw new IllegalArgumentException("El estilista no está disponible en la fecha y hora seleccionadas.");
        }

        existingAppointment.setStylist(stylist);
        existingAppointment.setService(service);
        existingAppointment.setAppointmentDateTime(appointmentDto.getAppointmentDateTime());
        existingAppointment.setNotes(appointmentDto.getNotes());
        // El estado no se cambia directamente aquí, sino por acciones específicas

        // Validar la fecha antes de guardar
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(existingAppointment, "appointment");
        appointmentDateTimeValidator.validate(existingAppointment, errors);
        if (errors.hasErrors()) {
            String errorMessage = errors.getFieldError() != null ? 
                errors.getFieldError().getDefaultMessage() : 
                "Error de validación en la fecha de la cita";            
            throw new IllegalArgumentException(errorMessage);
        }

        return appointmentRepository.save(existingAppointment);
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, User cancellingUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        boolean isAdmin = cancellingUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        boolean isClientOwner = appointment.getClient().getId().equals(cancellingUser.getId());
        boolean isStylistAssigned = appointment.getStylist().getId().equals(cancellingUser.getId());

        if (!isAdmin && !isClientOwner && !isStylistAssigned) {
            throw new AccessDeniedException("No tiene permisos para cancelar esta cita.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("No se puede cancelar una cita ya completada.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED_BY_ADMIN || appointment.getStatus() == AppointmentStatus.CANCELLED_BY_CLIENT || appointment.getStatus() == AppointmentStatus.CANCELLED_BY_STYLIST) {
            throw new IllegalStateException("La cita ya está cancelada.");
        }

        if (isAdmin) {
            appointment.setStatus(AppointmentStatus.CANCELLED_BY_ADMIN);
        } else if (isStylistAssigned) {
            appointment.setStatus(AppointmentStatus.CANCELLED_BY_STYLIST);
        } else { // isClientOwner
            appointment.setStatus(AppointmentStatus.CANCELLED_BY_CLIENT);
        }
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long id, User stylistUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        // Verificar que el usuario sea el estilista asignado o un admin
        boolean isAdmin = stylistUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!appointment.getStylist().getId().equals(stylistUser.getId()) && !isAdmin) {
            throw new AccessDeniedException("Solo el estilista asignado o un administrador pueden marcar la cita como completada.");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se pueden completar citas que están agendadas. Estado actual: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStylistAvailable(Long stylistId, LocalDateTime desiredDateTime, int durationMinutes, Long excludingAppointmentId) {
        User stylist = userRepository.findById(stylistId)
                .orElseThrow(() -> new IllegalArgumentException("Estilista no encontrado con ID: " + stylistId));

        LocalDateTime desiredEndTime = desiredDateTime.plusMinutes(durationMinutes);

        // Obtener todas las citas del estilista para ese día podrían ser muchas.
        // Sería más eficiente si la consulta al repositorio pudiera filtrar por un rango de tiempo más ajustado.
        // Por simplicidad aquí, filtramos en memoria después de obtener citas en un rango amplio si es necesario.
        // Idealmente, el repositorio tendría un método como:
        // findByStylistAndAppointmentDateTimeBetween(stylist, desiredDateTime.minusHours(X), desiredDateTime.plusHours(Y))
        
        List<Appointment> stylistAppointments = appointmentRepository
            .findByStylistAndAppointmentDateTimeBetween(stylist, desiredDateTime.toLocalDate().atStartOfDay(), desiredDateTime.toLocalDate().plusDays(1).atStartOfDay());

        for (Appointment existingAppointment : stylistAppointments) {
            if (excludingAppointmentId != null && existingAppointment.getId().equals(excludingAppointmentId)) {
                continue; // No comparar la cita consigo misma si se está actualizando
            }
            if (existingAppointment.getStatus() == AppointmentStatus.SCHEDULED || existingAppointment.getStatus() == AppointmentStatus.COMPLETED) { // Considerar citas completadas como ocupadas
                LocalDateTime existingStart = existingAppointment.getAppointmentDateTime();
                LocalDateTime existingEnd = existingStart.plusMinutes(existingAppointment.getService().getDurationMinutes());

                // Verifica si hay superposición
                // Nueva cita empieza durante una existente: (desiredStart >= existingStart && desiredStart < existingEnd)
                // Nueva cita termina durante una existente: (desiredEnd > existingStart && desiredEnd <= existingEnd)
                // Nueva cita envuelve una existente: (desiredStart <= existingStart && desiredEnd >= existingEnd)
                if ((desiredDateTime.isBefore(existingEnd) && desiredEndTime.isAfter(existingStart))) {
                    return false; // Hay superposición
                }
            }
        }
        return true; // No hay superposición, el estilista está disponible
    }
}
