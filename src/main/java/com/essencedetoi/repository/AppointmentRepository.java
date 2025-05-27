package com.essencedetoi.repository;

import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.AppointmentStatus;
import com.essencedetoi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT a FROM Appointment a WHERE a.client = :client ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByClient(@Param("client") User client);
    
    @Query("SELECT a FROM Appointment a WHERE a.client.id = :clientId ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT a FROM Appointment a WHERE a.stylist = :stylist ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByStylist(@Param("stylist") User stylist);
    
    @Query("SELECT a FROM Appointment a WHERE a.stylist.id = :stylistId ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByStylistId(@Param("stylistId") Long stylistId);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :start AND :end ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByAppointmentDateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM Appointment a WHERE a.stylist = :stylist AND a.appointmentDateTime BETWEEN :start AND :end ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByStylistAndAppointmentDateTimeBetween(
        @Param("stylist") User stylist, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.client = :client AND a.appointmentDateTime BETWEEN :start AND :end ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findByClientAndAppointmentDateTimeBetween(
        @Param("client") User client, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    List<Appointment> findByStatus(AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a ORDER BY a.createdAt DESC")
    List<Appointment> findAllByOrderByCreatedAtDesc();
}
