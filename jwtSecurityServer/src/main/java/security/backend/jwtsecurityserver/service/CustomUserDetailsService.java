package security.backend.jwtsecurityserver.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import security.backend.jwtsecurityserver.model.UserDAO;
import security.backend.jwtsecurityserver.model.UserDTO;
import security.backend.jwtsecurityserver.repository.UserRepository;

import java.util.List;

/**
 * User service
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder bcryptEncoder;

    public CustomUserDetailsService(UserRepository repository, PasswordEncoder bcryptEncoder) {
        this.repository = repository;
        this.bcryptEncoder = bcryptEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> roles;
        UserDAO user = repository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("User not found with the name " + username);

        roles = List.of(new SimpleGrantedAuthority(user.getRole()));
        return new User(user.getUsername(), user.getPassword(), roles);
    }

    public UserDAO save(UserDTO user) {
        UserDAO newUser = new UserDAO();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        newUser.setRole(user.getRole());
        return repository.save(newUser);
    }
}
