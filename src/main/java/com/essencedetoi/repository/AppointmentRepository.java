package com.essencedetoi.repository;

import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.AppointmentStatus;
import com.essencedetoi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClient(User client);
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByStylist(User stylist);
    List<Appointment> findByStylistId(Long stylistId);
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByStylistAndAppointmentDateTimeBetween(User stylist, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByClientAndAppointmentDateTimeBetween(User client, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByStatus(AppointmentStatus status);
}
