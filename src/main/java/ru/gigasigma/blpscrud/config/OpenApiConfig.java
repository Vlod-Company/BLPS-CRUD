package ru.gigasigma.blpscrud.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BLPS CRUD API",
                version = "v1",
                description = "API for flight search, purchases, orders, and ticket delivery.",
                contact = @Contact(name = "BLPS CRUD"),
                license = @License(name = "Internal use")
        ),
        servers = {
                @Server(url = "/", description = "Current server")
        },
        security = {
                @SecurityRequirement(name = "basicAuth")
        },
        tags = {
                @Tag(name = "Authentication", description = "User registration and authentication-related operations"),
                @Tag(name = "Flights", description = "Flight search and flight details"),
                @Tag(name = "Internal Purchases", description = "Internal purchase creation and internal payment callbacks"),
                @Tag(name = "External Purchases", description = "External airline purchase integration"),
                @Tag(name = "Orders", description = "Order retrieval and lifecycle operations"),
                @Tag(name = "Tickets", description = "Ticket retrieval and PDF download")
        }
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic authentication"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI blpsOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        "basicAuth",
                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .in(In.HEADER)
                ))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("basicAuth"))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("BLPS CRUD API")
                        .version("v1")
                        .description("Runtime-generated OpenAPI documentation for the BLPS CRUD service.")
                        .contact(new io.swagger.v3.oas.models.info.Contact().name("BLPS CRUD"))
                        .license(new io.swagger.v3.oas.models.info.License().name("Internal use")));
    }
}
