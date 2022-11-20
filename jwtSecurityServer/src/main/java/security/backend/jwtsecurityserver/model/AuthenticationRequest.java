package security.backend.jwtsecurityserver.model;

import lombok.Data;

/**
 * Expected information for authentication
 */
@Data
public class AuthenticationRequest {
    private String username;
    private String password;
}
