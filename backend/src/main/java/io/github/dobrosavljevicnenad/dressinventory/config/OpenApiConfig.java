package io.github.dobrosavljevicnenad.dressinventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description served at /v3/api-docs, browsable in Swagger UI at /swagger-ui.html.
 * "Authorize" in Swagger UI accepts the token returned by POST /api/login.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    OpenAPI dressInventoryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dress Inventory API")
                        .version("v1")
                        .description("Inventory and order management for a dress boutique: catalog, "
                                + "stock per color, concurrency-safe ordering and image uploads."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
