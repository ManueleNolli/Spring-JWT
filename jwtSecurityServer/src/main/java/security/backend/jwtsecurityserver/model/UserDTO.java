package security.backend.jwtsecurityserver.model;


import lombok.Data;

/**
 * Class used for communication with client
 */
@Data
public class UserDTO {
    private String username;
    private String password;
    private String role;
}
