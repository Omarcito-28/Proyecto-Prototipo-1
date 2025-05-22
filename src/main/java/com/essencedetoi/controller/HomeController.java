package com.essencedetoi.controller;

import com.essencedetoi.model.Service;
import com.essencedetoi.service.SalonServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final SalonServiceService salonServiceService;

    @Autowired
    public HomeController(SalonServiceService salonServiceService) {
        this.salonServiceService = salonServiceService;
    }

    @GetMapping("/")
    public String home(Model model) {
        logger.debug("Cargando página de inicio");
        
        try {
            List<Service> services = salonServiceService.getAllServices();
            model.addAttribute("services", services);
            
            // Mensajes estáticos (puedes reemplazarlos con mensajes de la base de datos si es necesario)
            model.addAttribute("welcomeMessage", "Bienvenido a Essence de Toi");
            model.addAttribute("pageTitle", "Inicio - Essence de Toi");
            
            logger.debug("Página de inicio cargada con éxito");
            return "index";
        } catch (Exception e) {
            logger.error("Error al cargar la página de inicio: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar la página de inicio");
            return "error";
        }
    }

    @GetMapping("/home")
    public String homeAlias(Model model) {
        return home(model);
    }
}
