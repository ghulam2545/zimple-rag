package com.ghulam.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Zimple RAG",
                version = "1.0.0",
                description = "An RAG system for your markdown files",
                contact = @Contact(
                        name = "ghulam2545@gmail.com"
                )
        )
)
@Configuration
public class SwaggerConfiguration {
}
