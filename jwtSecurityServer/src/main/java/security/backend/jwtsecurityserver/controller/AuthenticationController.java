package security.backend.jwtsecurityserver.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import security.backend.jwtsecurityserver.model.*;
import security.backend.jwtsecurityserver.service.CustomUserDetailsService;
import security.backend.jwtsecurityserver.service.JwtService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;


    public AuthenticationController(AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/authenticate")
    public ResponseEntity<AuthenticationResponse> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        UserDetails userdetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        String accessToken = jwtService.generateToken(userdetails,"access");
        String refreshToken = jwtService.generateToken(userdetails,"refresh");
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(accessToken, refreshToken);
        System.out.println(authenticationResponse.getAccessToken() + " " + authenticationResponse.getRefreshToken());
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserDAO> saveUser(@RequestBody UserDTO user) {
        return ResponseEntity.ok(userDetailsService.save(user));
    }


    @PostMapping(value = "/refreshtoken")
    public ResponseEntity<RequestRefreshToken> refreshToken(@RequestBody RequestRefreshToken refreshToken) {
        Claims claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(refreshToken.getRefreshToken()).getBody();

        String username = claims.getSubject();

        RequestRefreshToken response = new RequestRefreshToken();
        response.setRefreshToken(jwtService.generateToken(userDetailsService.loadUserByUsername(username),"access"));
        return ResponseEntity.ok(response);
    }

}
