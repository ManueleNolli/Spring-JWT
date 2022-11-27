package security.frontend.jwtsecurityclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import security.frontend.jwtsecurityclient.Model.ResponseToken;
import security.frontend.jwtsecurityclient.Model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Controller
public class ClientController {
    private final RestTemplate restTemplate;

    private static final String REGISTRATION_URL = "http://localhost:8080/register";
    private static final String AUTHENTICATION_URL = "http://localhost:8080/authenticate";
    private static final String REFRESH_TOKEN = "http://localhost:8080/refreshtoken";

    private static final String HOME_URL = "redirect:/home";
    private static final String LOGIN_URL = "redirect:/";
    private static final String ERROR_URL = "redirect:/error";
    private String token;

    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String login(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request) {
        getToken(request);
        if (token == null) {
            return ERROR_URL;
        }
        System.out.println("Token: " + token);
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
    public String userPage() {
        return "userPage";
    }

    @GetMapping("/adminPage")
    public String adminPage(HttpServletRequest request) {
        getToken(request);
        if (token == null) {
            return ERROR_URL;
        }
        System.out.println("Token: " + token);
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

            token = Objects.requireNonNull(loginResponse.getBody()).getToken();
            if (loginResponse.getStatusCode().equals(HttpStatus.OK)) {
                Cookie jwtTokenCookie = new Cookie("Authorization",token );
                jwtTokenCookie.setHttpOnly(true);
                jwtTokenCookie.setSecure(true);
                jwtTokenCookie.setPath("/");
                jwtTokenCookie.setMaxAge(3600);
                responseHttp.addCookie(jwtTokenCookie);
                response = HOME_URL;
            } else {
                response = ERROR_URL;
            }
        } catch (Exception e) {
            response = ERROR_URL;
        }
        return response;
    }

        private String getToken(HttpServletRequest request) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("Authorization")) {
                        token = cookie.getValue();
                    }
                }
            }
            return token;
        }

/*    private String getData() {
        String response = null;

        HttpHeaders headers = getHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
        // Use Token to get Response
        ResponseEntity<String> helloResponse = restTemplate.exchange(HOME_URL, HttpMethod.GET, jwtEntity,
                String.class);
        if (helloResponse.getStatusCode().equals(HttpStatus.OK)) {
            response = helloResponse.getBody();
        }
        return response;
    }*/

    private void refreshToken() {
        HttpHeaders headers = getHeaders();
        headers.set("Authorization", token);
        headers.set("isRefreshToken", "true");
        HttpEntity<String> jwtEntity = new HttpEntity<>(headers);
        // Use Token to get Response
        ResponseEntity<ResponseToken> refreshTokenResponse = restTemplate.exchange(REFRESH_TOKEN, HttpMethod.GET, jwtEntity,
                ResponseToken.class);
        if (refreshTokenResponse.getStatusCode().equals(HttpStatus.OK)) {
            token = "Bearer " + Objects.requireNonNull(refreshTokenResponse.getBody()).getToken();
        }
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
}
