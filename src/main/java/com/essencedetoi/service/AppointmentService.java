package com.essencedetoi.service;

import com.essencedetoi.dto.AppointmentRequestDto;
import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    Appointment createAppointment(AppointmentRequestDto appointmentDto, User client);
    Optional<Appointment> getAppointmentById(Long id);
    List<Appointment> getAllAppointments();
    List<Appointment> getAppointmentsForClient(Long clientId);
    List<Appointment> getAppointmentsForStylist(Long stylistId);
    List<Appointment> getAppointmentsForStylistBetweenDates(Long stylistId, LocalDateTime start, LocalDateTime end);
    List<Appointment> getAppointmentsForClientBetweenDates(Long clientId, LocalDateTime start, LocalDateTime end);
    Appointment updateAppointment(Long id, AppointmentRequestDto appointmentDto, User requestingUser);
    Appointment cancelAppointment(Long id, User cancellingUser);
    Appointment completeAppointment(Long id, User stylistUser);
    boolean isStylistAvailable(Long stylistId, LocalDateTime dateTime, int durationMinutes, Long excludingAppointmentId);
    
    // Métodos para el dashboard
    List<Appointment> getTodayAppointmentsForStylist(Long stylistId);
    List<Appointment> getUpcomingAppointmentsForClient(Long clientId);
    long countCompletedAppointmentsForStylist(Long stylistId);
}
