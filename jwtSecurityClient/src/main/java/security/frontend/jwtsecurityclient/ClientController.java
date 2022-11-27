package security.frontend.jwtsecurityclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;
import security.frontend.jwtsecurityclient.Model.RequestRefreshToken;
import security.frontend.jwtsecurityclient.Model.ResponseToken;
import security.frontend.jwtsecurityclient.Model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class ClientController {
    private final RestTemplate restTemplate;

    private static final String REGISTRATION_URL = "http://localhost:8080/register";
    private static final String AUTHENTICATION_URL = "http://localhost:8080/authenticate";
    private static final String REFRESH_TOKEN = "http://localhost:8080/refreshtoken";

    private static final String HOME_URL = "redirect:/home";
    private static final String LOGIN_URL = "redirect:/";
    private static final String ERROR_URL = "redirect:/error";


    private String secret;
    private int jwtExpirationInMs;
    private int refreshExpirationDateInMs;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Value("${jwt.expirationDateInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    @Value("${jwt.refreshExpirationDateInMs}")
    public void setRefreshExpirationDateInMs(int refreshExpirationDateInMs) {
        this.refreshExpirationDateInMs = refreshExpirationDateInMs;
    }

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String login(Model model, HttpServletResponse response) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        if(isRefreshNeeded(request)) {
            refreshToken(request, response);
        }
        return "home";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/userPage")
    public String userPage(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        if(isRefreshNeeded(request)) {
            refreshToken(request, response);
        }
        return "userPage";
    }

    @GetMapping("/adminPage")
    public String adminPage(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        if(isRefreshNeeded(request)) {
            refreshToken(request, response);
        }
        return "adminPage";
    }

    @PostMapping("/registration")
    public String registration(User user) throws JsonProcessingException {
        String response = null;
        // convert the user registration object to JSON
        String registrationBody = getBody(user);

        // create headers specifying that it is JSON request
        HttpHeaders registrationHeaders = getHeaders();
        HttpEntity<String> registrationEntity = new HttpEntity<>(registrationBody, registrationHeaders);

        try {
            // Register User
            ResponseEntity<String> registrationResponse = restTemplate.exchange(REGISTRATION_URL, HttpMethod.POST,
                    registrationEntity, String.class);

            if (registrationResponse.getStatusCode().equals(HttpStatus.OK)) {
                response = LOGIN_URL;
            } else {
                response = ERROR_URL;
            }
        } catch (Exception e) {
            response = ERROR_URL;
        }
        return response;
    }

    @PostMapping("/login")
    public String handleLogin(User user, HttpServletResponse responseHttp) throws JsonProcessingException {
        String response;
        // convert the user registration object to JSON
        String loginBody = getBody(user);

        // create headers specifying that it is JSON request
        HttpHeaders loginHeaders = getHeaders();
        HttpEntity<String> loginEntity = new HttpEntity<>(loginBody, loginHeaders);
        try {
            // Login User
            ResponseEntity<ResponseToken> loginResponse = restTemplate.exchange(AUTHENTICATION_URL, HttpMethod.POST,
                    loginEntity, ResponseToken.class);

            String accesstoken = Objects.requireNonNull(loginResponse.getBody()).getAccessToken();
            String refreshtoken = Objects.requireNonNull(loginResponse.getBody()).getRefreshToken();
            if (loginResponse.getStatusCode().equals(HttpStatus.OK)) {
                Cookie jwtAccessTokenCookie = new Cookie("accessToken", accesstoken);
                jwtAccessTokenCookie.setHttpOnly(true);
                jwtAccessTokenCookie.setSecure(true);
                jwtAccessTokenCookie.setPath("/");
                jwtAccessTokenCookie.setMaxAge(jwtExpirationInMs/1000); //ms to seconds
                responseHttp.addCookie(jwtAccessTokenCookie);

                Cookie jwtRefreshTokenCookie = new Cookie("refreshToken", refreshtoken);
                jwtRefreshTokenCookie.setHttpOnly(true);
                jwtRefreshTokenCookie.setSecure(true);
                jwtRefreshTokenCookie.setPath("/");
                jwtRefreshTokenCookie.setMaxAge(refreshExpirationDateInMs/1000); //ms to seconds
                responseHttp.addCookie(jwtRefreshTokenCookie);

                response = HOME_URL;
            } else {
                response = ERROR_URL;
            }
        } catch (Exception e) {
            response = ERROR_URL;
        }
        return response;
    }

    private boolean isRefreshNeeded(HttpServletRequest request) {
        Boolean b = (Boolean) request.getAttribute("isRefreshNeeded");
        return b != null && b;
    }

    private void refreshToken(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        // convert the user registration object to JSON
        RequestRefreshToken refreshToken = new RequestRefreshToken(request.getAttribute("refreshToken").toString());
        String refreshBody = getBody(refreshToken);

        // create headers specifying that it is JSON request
        HttpHeaders headers = getHeaders();
        HttpEntity<String> refreshEntity = new HttpEntity<>(refreshBody, headers);

        ResponseEntity<RequestRefreshToken> refreshResponse = restTemplate.exchange(REFRESH_TOKEN, HttpMethod.POST,
                refreshEntity, RequestRefreshToken.class);

        String accesstoken = Objects.requireNonNull(refreshResponse.getBody()).getRefreshToken();
        System.out.println("new access token" + accesstoken);
        Cookie jwtAccessTokenCookie = new Cookie("accessToken", accesstoken);
        jwtAccessTokenCookie.setHttpOnly(true);
        jwtAccessTokenCookie.setSecure(true);
        jwtAccessTokenCookie.setPath("/");
        jwtAccessTokenCookie.setMaxAge(jwtExpirationInMs/1000); //ms to seconds
        response.addCookie(jwtAccessTokenCookie);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String getBody(final User user) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(user);
    }

    private String getBody(final RequestRefreshToken refreshToken) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(refreshToken);
    }
}
