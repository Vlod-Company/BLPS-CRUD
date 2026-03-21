package ru.gigasigma.blpscrud.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BLPS CRUD API",
                version = "v1",
                description = "API for flight search, booking, payments, and ticket delivery.",
                contact = @Contact(name = "BLPS CRUD"),
                license = @License(name = "Internal use")
        ),
        servers = {
                @Server(url = "/", description = "Current server")
        },
        tags = {
                @Tag(name = "Flights", description = "Flight search and flight details"),
                @Tag(name = "Orders", description = "Order creation and lifecycle operations"),
                @Tag(name = "Payments", description = "Payment provider callbacks"),
                @Tag(name = "Booking", description = "External booking provider integration"),
                @Tag(name = "Tickets", description = "Ticket details and PDF download")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI blpsOpenApi() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("BLPS CRUD API")
                        .version("v1")
                        .description("Runtime-generated OpenAPI documentation for the BLPS CRUD service.")
                        .contact(new io.swagger.v3.oas.models.info.Contact().name("BLPS CRUD"))
                        .license(new io.swagger.v3.oas.models.info.License().name("Internal use")));
    }
}
