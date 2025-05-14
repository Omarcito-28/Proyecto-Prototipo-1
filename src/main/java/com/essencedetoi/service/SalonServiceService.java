package com.essencedetoi.service;

import com.essencedetoi.dto.ServiceDto;
import com.essencedetoi.model.Service; // La entidad Service

import java.util.List;
import java.util.Optional;

public interface SalonServiceService {
    Service createService(ServiceDto serviceDto);
    Optional<Service> getServiceById(Long id);
    List<Service> getAllServices();
    Service updateService(Long id, ServiceDto serviceDto);
    void deleteService(Long id);
    Optional<Service> findByName(String name);
    Service save(Service service); // Nuevo método para guardar directamente
}
