package com.essencedetoi.service.impl;

import com.essencedetoi.dto.ServiceDto;
import com.essencedetoi.model.Service; // La entidad Service
import com.essencedetoi.repository.ServiceRepository;
import com.essencedetoi.service.SalonServiceService;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service; // Esta anotación debe ser @Service
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service // Asegurando que sea un bean de servicio
public class SalonServiceServiceImpl implements SalonServiceService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public SalonServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    @Transactional
    public Service createService(ServiceDto serviceDto) {
        if (serviceRepository.findByName(serviceDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un servicio con el nombre: " + serviceDto.getName());
        }
        Service newService = new Service();
        newService.setName(serviceDto.getName());
        newService.setDescription(serviceDto.getDescription());
        newService.setPrice(serviceDto.getPrice());
        newService.setDurationMinutes(serviceDto.getDurationMinutes());
        return serviceRepository.save(newService);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    @Transactional
    public Service updateService(Long id, ServiceDto serviceDto) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + id));

        // Verificar si el nuevo nombre ya existe para otro servicio
        Optional<Service> serviceWithNewName = serviceRepository.findByName(serviceDto.getName());
        if (serviceWithNewName.isPresent() && !serviceWithNewName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Otro servicio ya existe con el nombre: " + serviceDto.getName());
        }

        existingService.setName(serviceDto.getName());
        existingService.setDescription(serviceDto.getDescription());
        existingService.setPrice(serviceDto.getPrice());
        existingService.setDurationMinutes(serviceDto.getDurationMinutes());
        return serviceRepository.save(existingService);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Servicio no encontrado con id: " + id);
        }
        // Considerar si hay citas asociadas antes de eliminar o manejarlo con la BD (ON DELETE)
        serviceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> findByName(String name) {
        return serviceRepository.findByName(name);
    }

    @Override
    @Transactional
    public Service save(Service service) {
        return serviceRepository.save(service);
    }
}
