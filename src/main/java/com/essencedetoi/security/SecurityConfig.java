package com.essencedetoi.security;

import com.essencedetoi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true) // Habilitar seguridad a nivel de método
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Habilitar CSRF excepto para endpoints API\
                .authorizeHttpRequests(authorize -> authorize
                        // Rutas públicas
                        .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/register", "/register/save").permitAll()
                        .requestMatchers("/login", "/login-error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll() // Permitir acceso a Swagger y su config

                        // Rutas de Administrador (más restrictivas, procesadas antes que las generales para /appointments/**)
                        .requestMatchers("/admin/**", "/users/**", "/services/new", "/services/edit/**", "/services/delete/**", "/appointments/all").hasRole("ADMIN")
                        
                        // Rutas que requieren autenticación general
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/appointments/**").authenticated() // Para citas no admin (e.g., /my-appointments, /details/*, /schedule)
                        .requestMatchers("/stylist/**").authenticated()
                        .requestMatchers("/profile/**").authenticated() // Proteger rutas de perfil

                        // Rutas con roles específicos para ver lista de servicios
                        .requestMatchers("/services/", "/services/list").hasAnyRole("ADMIN", "STYLIST", "CLIENT")
                        
                        .anyRequest().authenticated() // Todas las demás no listadas explícitamente requieren autenticación
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // URL a la que se envía el formulario de login
                        .defaultSuccessUrl("/", true) // Redirigir después del login exitoso
                        .failureUrl("/login-error") // Página de error de login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }
}
