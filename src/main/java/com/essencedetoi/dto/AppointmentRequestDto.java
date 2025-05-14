package com.essencedetoi.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AppointmentRequestDto {

    private Long id; // Para la edición de citas

    private Long clientId; // El cliente se tomará del usuario autenticado generalmente

    @NotNull(message = "El ID del estilista es obligatorio")
    private Long stylistId;

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long serviceId;

    @NotNull(message = "La fecha y hora de la cita son obligatorias")
    @Future(message = "La cita debe ser en una fecha y hora futuras")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Asegura el formato correcto desde el formulario
    private LocalDateTime appointmentDateTime;

    private String notes;

    // No incluimos el estado aquí, ya que se gestionará internamente
    // o a través de acciones específicas (completar, cancelar).
}
