package com.essencedetoi.controller;

import com.essencedetoi.model.Service;
import com.essencedetoi.service.SalonServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final SalonServiceService salonServiceService;

    @Autowired
    public HomeController(SalonServiceService salonServiceService) {
        this.salonServiceService = salonServiceService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Service> services = salonServiceService.getAllServices();
        model.addAttribute("services", services);
        model.addAttribute("pageTitle", "Bienvenido a Essence De Toi");
        return "index"; // Corresponde a src/main/resources/templates/index.html
    }

    // Si tienes una página /home separada o como alias, también puedes manejarla aquí
    @GetMapping("/home")
    public String homeAlias(Model model) {
        return home(model); // Reutiliza la lógica del método home
    }
}
