package security.backend.jwtsecurityserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import security.backend.jwtsecurityserver.model.UserDAO;

@SpringBootApplication
public class JwtSecurityServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtSecurityServerApplication.class, args);
        UserDAO userDAO = new UserDAO();
        userDAO.setUsername("admin");
    }

}
