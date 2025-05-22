package com.essencedetoi.validation;

import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.AppointmentStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class AppointmentDateTimeValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Appointment.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        Appointment appointment = (Appointment) target;
        LocalDateTime appointmentDateTime = appointment.getAppointmentDateTime();
        LocalDateTime now = LocalDateTime.now();

        // Validar que la cita sea en el futuro
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED && 
            appointmentDateTime.isBefore(now)) {
            errors.rejectValue("appointmentDateTime", "appointment.datetime.future",
                    "La fecha de la cita debe ser en el futuro");
            return;
        }
        
        // Validar horario comercial (9 AM a 7 PM)
        if (appointmentDateTime != null) {
            LocalTime appointmentTime = appointmentDateTime.toLocalTime();
            LocalTime openingTime = LocalTime.of(9, 0);  // 9:00 AM
            LocalTime closingTime = LocalTime.of(19, 0);  // 7:00 PM
            
            if (appointmentTime.isBefore(openingTime) || 
                !appointmentTime.plusMinutes(appointment.getService().getDurationMinutes()).isBefore(closingTime.plusSeconds(1))) {
                errors.rejectValue("appointmentDateTime", "appointment.datetime.business.hours",
                        String.format("Las citas deben estar dentro del horario de atención: %s a %s", 
                                openingTime, closingTime));
            }
        }
    }
}
