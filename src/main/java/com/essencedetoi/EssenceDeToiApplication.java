package com.essencedetoi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Essence De Toi API", version = "1.0", description = "API para la gestión de citas del Salón de Belleza Essence De Toi"))
public class EssenceDeToiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EssenceDeToiApplication.class, args);
    }

}
