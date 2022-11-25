package security.backend.jwtsecurityserver.model;

import lombok.*;

/**
 * Response after authentication
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String token;
}
