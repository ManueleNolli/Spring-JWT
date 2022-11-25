package security.backend.jwtsecurityserver.model;

import lombok.*;

/**
 * Expected information for authentication
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    private String username;
    private String password;
}
