package com.essencedetoi.controller;

import com.essencedetoi.dto.ServiceDto;
import com.essencedetoi.model.Service;
import com.essencedetoi.service.SalonServiceService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/services")
public class SalonServiceController {

    private final SalonServiceService salonServiceService;

    @Autowired
    public SalonServiceController(SalonServiceService salonServiceService) {
        this.salonServiceService = salonServiceService;
    }

    // Lista de servicios (accesible para todos)
    @GetMapping({"/", "/list"})
    public String listServices(Model model) {
        List<Service> services = salonServiceService.getAllServices();
        model.addAttribute("services", services);
        return "service/service_list";
    }

    // Actualizar precio del servicio (solo ADMIN)
    @PostMapping("/update-price/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateServicePrice(@PathVariable Long id, @RequestParam BigDecimal price) {
        try {
            System.out.println("Actualizando precio para servicio ID: " + id + " a: " + price);
            
            Service service = salonServiceService.getServiceById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
            
            System.out.println("Precio actual: " + service.getPrice());
            service.setPrice(price);
            System.out.println("Nuevo precio establecido: " + service.getPrice());
            
            Service updatedService = salonServiceService.save(service);
            System.out.println("Servicio guardado. Precio después de guardar: " + updatedService.getPrice());
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedService.getId());
            response.put("price", updatedService.getPrice());
            response.put("name", updatedService.getName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error al actualizar precio: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private ServiceDto convertToDto(Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setPrice(service.getPrice());
        return dto;
    }
}
