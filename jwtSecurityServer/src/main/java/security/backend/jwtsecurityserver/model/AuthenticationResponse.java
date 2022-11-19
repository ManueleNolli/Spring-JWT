package security.backend.jwtsecurityserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response after authentication
 */
@Data @AllArgsConstructor
public class AuthenticationResponse {

	private String token;

}
