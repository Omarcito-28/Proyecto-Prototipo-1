package com.essencedetoi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;
import java.util.Map;

@Controller
public class LocaleController {

    // Lista de idiomas soportados
    private static final Map<String, Locale> SUPPORTED_LOCALES = Map.of(
        "es", Locale.forLanguageTag("es-ES"),
        "en", Locale.ENGLISH,
        "pt", Locale.forLanguageTag("pt-PT")
    );

    /**
     * Maneja el cambio de idioma en la aplicación
     * @param lang Código del idioma (ej: es, en, pt)
     * @param request Objeto HttpServletRequest
     * @param response Objeto HttpServletResponse
     * @return Redirige a la página de origen o a la página de inicio
     */
    @GetMapping("/change-language")
    public String changeLanguage(
            @RequestParam(name = "lang") String lang,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Obtener el locale solicitado o usar el español por defecto
        Locale locale = SUPPORTED_LOCALES.getOrDefault(lang, SUPPORTED_LOCALES.get("es"));
        
        // Obtener el resolvedor de localización
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        
        if (localeResolver != null) {
            // Establecer el nuevo locale
            localeResolver.setLocale(request, response, locale);
        }
        
        // Redirigir a la página de origen o a la página de inicio
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
