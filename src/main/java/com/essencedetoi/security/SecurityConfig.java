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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

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
        logger.info("Configurando SecurityFilterChain...");
        
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/**",
                    "/webjars/**",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ))
                .authorizeHttpRequests(authorize -> authorize
                    // Rutas públicas
                    .requestMatchers(
                        "/",
                        "/home",
                        "/register",
                        "/register/save",
                        "/login",
                        "/login/**",
                        "/logout",
                        "/webjars/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/change-language",
                        "/services/list",
                        "/services/{id}",
                        "/appointments/available-times",
                        "/appointments/new",
                        "/appointments/save"
                    ).permitAll()
                    
                    // Rutas de administración
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    
                    // Rutas de usuario autenticado
                    .requestMatchers("/profile/**", "/appointments/my").authenticated()
                    
                    // Todas las demás rutas requieren autenticación
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login-error")
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
