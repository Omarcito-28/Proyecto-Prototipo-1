package com.essencedetoi.validation;

import com.essencedetoi.model.Appointment;
import com.essencedetoi.model.AppointmentStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class AppointmentDateTimeValidator implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Appointment.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        Appointment appointment = (Appointment) target;
        LocalDateTime now = LocalDateTime.now();

        // Solo validar que la fecha sea futura si la cita está siendo creada o actualizada
        // y no está siendo marcada como completada o cancelada
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED && 
            appointment.getAppointmentDateTime().isBefore(now)) {
            errors.rejectValue("appointmentDateTime", "appointment.datetime.future",
                    "La fecha de la cita debe ser en el futuro");
        }
    }
}
