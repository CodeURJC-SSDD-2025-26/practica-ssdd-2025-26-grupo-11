package es.codeurjc.practica2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI biblioOnlineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BiblioOnline API")
                        .description("API REST para la gestión de libros, préstamos, reseñas y usuarios.")
                        .version("1.0.0"));
    }
}