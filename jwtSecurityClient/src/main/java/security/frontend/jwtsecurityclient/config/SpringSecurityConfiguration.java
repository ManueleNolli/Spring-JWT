package security.frontend.jwtsecurityclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {
    private final CustomJwtAuthenticationFilter customJwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SpringSecurityConfiguration(CustomJwtAuthenticationFilter customJwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.customJwtAuthenticationFilter = customJwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Enable CORS support
        http.cors();
        // Disable CSRF support
        http.csrf().disable();
        // Authorize requests
        // Each matcher is considered in the order they were declared
        http.authorizeRequests()
                .antMatchers().hasRole("USER")
                .antMatchers("/adminPage").hasRole("ADMIN")
                .antMatchers("/home","/userPage").hasAnyRole("USER", "ADMIN")
                .antMatchers("/registration","/","/login").permitAll()
                .anyRequest().authenticated();
        // Define the exception handler for unauthorized requests
        http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
        // Disable session creation
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // Add a filter to validate the tokens with every request
        http.addFilterBefore(customJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // Return the security filter chain
        return http.build();
    }
}

