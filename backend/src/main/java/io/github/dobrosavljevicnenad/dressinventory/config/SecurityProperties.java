package io.github.dobrosavljevicnenad.dressinventory.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Security settings, validated at startup: the application refuses to start
 * with a missing admin password or a weak JWT secret.
 */
@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(
        @Valid @NotNull Admin admin,
        @Valid @NotNull Jwt jwt,
        @NotEmpty List<String> corsAllowedOrigins) {

    public record Admin(
            @NotBlank String username,
            @NotBlank
            @Pattern(regexp = "^\\{[a-z0-9]+}.+$",
                    message = "must start with an encoder id, e.g. {bcrypt}<hash> or {noop}<plain-text>")
            String password) {
    }

    public record Jwt(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters long") String secret,
            @DefaultValue("2h") Duration ttl,
            @DefaultValue("dress-inventory") String issuer) {
    }
}
