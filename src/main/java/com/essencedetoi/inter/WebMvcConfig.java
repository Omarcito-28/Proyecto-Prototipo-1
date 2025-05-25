package com.essencedetoi.inter;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configura el resolvedor de localización para usar la sesión
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        // Establece el idioma por defecto
        resolver.setDefaultLocale(Locale.forLanguageTag("es-ES"));
        // Configura el nombre de la cookie
        resolver.setCookieName("lang");
        // Establece el tiempo de vida de la cookie a 1 año (en segundos)
        resolver.setCookieMaxAge(31536000);
        // Hace la cookie accesible para todo el sitio
        resolver.setCookiePath("/");
        return resolver;
    }

    /**
     * Interceptor que detecta cambios de idioma a través del parámetro 'lang'
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Registra el interceptor
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
