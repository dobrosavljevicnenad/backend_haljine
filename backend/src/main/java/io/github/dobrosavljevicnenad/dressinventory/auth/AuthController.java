package io.github.dobrosavljevicnenad.dressinventory.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokens;

    AuthController(AuthenticationManager authenticationManager, TokenService tokens) {
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new TokenResponse(tokens.issueFor(authentication.getName()));
    }

    /** Reaching this endpoint already proves the token is valid (the security filter rejects others). */
    @GetMapping("/proveri-token")
    TokenCheckResponse checkToken() {
        return new TokenCheckResponse(true);
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    record TokenResponse(String token) {
    }

    record TokenCheckResponse(@JsonProperty("validan") boolean valid) {
    }
}
