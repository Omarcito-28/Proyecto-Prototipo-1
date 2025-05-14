package com.essencedetoi.model;

public enum AppointmentStatus {
    SCHEDULED,  // Agendada
    COMPLETED,  // Completada
    CANCELLED_BY_CLIENT, // Cancelada por el cliente
    CANCELLED_BY_STYLIST, // Cancelada por el estilista
    CANCELLED_BY_ADMIN // Cancelada por el administrador
}
