/*
package security.frontend.jwtsecurityclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ClientControllerRest {
    RestTemplate restTemplate;

    private static final String REGISTRATION_URL = "http://localhost:8080/register";
    private static final String AUTHENTICATION_URL = "http://localhost:8080/authenticate";
    private static final String HELLO_URL = "http://localhost:8080/helloadmin";
    private static final String REFRESH_TOKEN = "http://localhost:8080/refreshtoken";

    private String token;

    public ClientControllerRest(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(value = "/getResponse")
    public String getResponse() throws JsonProcessingException {

        String response = null;
        // create user registration object
        RegistrationUser registrationUser = getRegistrationUser();
        // convert the user registration object to JSON
        String registrationBody = getBody(registrationUser);
        // create headers specifying that it is JSON request
        HttpHeaders registrationHeaders = getHeaders();
        HttpEntity<String> registrationEntity = new HttpEntity<String>(registrationBody, registrationHeaders);

        try {
            // Register User
            ResponseEntity<String> registrationResponse = restTemplate.exchange(REGISTRATION_URL, HttpMethod.POST,
                    registrationEntity, String.class);
            // if the registration is successful
            if (registrationResponse.getStatusCode().equals(HttpStatus.OK)) {

                // create user authentication object
                User authenticationUser = getAuthenticationUser();
                // convert the user authentication object to JSON
                String authenticationBody = getBody(authenticationUser);
                // create headers specifying that it is JSON request
                HttpHeaders authenticationHeaders = getHeaders();
                HttpEntity<String> authenticationEntity = new HttpEntity<String>(authenticationBody,
                        authenticationHeaders);

                // Authenticate User and get JWT
                ResponseEntity<ResponseToken> authenticationResponse = restTemplate.exchange(AUTHENTICATION_URL,
                        HttpMethod.POST, authenticationEntity, ResponseToken.class);

                // if the authentication is successful
                if (authenticationResponse.getStatusCode().equals(HttpStatus.OK)) {
                    token = "Bearer " + authenticationResponse.getBody().getToken();
                    response = getData();

                }
            }
        } catch (Exception ex) {
            // check if exception is due to ExpiredJwtException
            if (ex.getMessage().contains("io.jsonwebtoken.ExpiredJwtException")) {
                // Refresh Token
                refreshToken();
                // try again with refresh token
                response = getData();
            }else {
                System.out.println(ex);
            }
        }
        return response;

    }

    private String getData() {
        String response = null;

        HttpHeaders headers = getHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
        // Use Token to get Response
        ResponseEntity<String> helloResponse = restTemplate.exchange(HELLO_URL, HttpMethod.GET, jwtEntity,
                String.class);
        if (helloResponse.getStatusCode().equals(HttpStatus.OK)) {
            response = helloResponse.getBody();
        }
        return response;

    }

    private void refreshToken() {
        HttpHeaders headers = getHeaders();
        headers.set("Authorization", token);
        headers.set("isRefreshToken", "true");
        HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
        // Use Token to get Response
        ResponseEntity<ResponseToken> refreshTokenResponse = restTemplate.exchange(REFRESH_TOKEN, HttpMethod.GET, jwtEntity,
                ResponseToken.class);
        if (refreshTokenResponse.getStatusCode().equals(HttpStatus.OK)) {
            token = "Bearer " +refreshTokenResponse.getBody().getToken();
        }

    }

    private RegistrationUser getRegistrationUser() {
        RegistrationUser user = new RegistrationUser();
        user.setUsername("javainuse");
        user.setPassword("javainuse");
        user.setRole("ROLE_ADMIN");
        return user;
    }

    private User getAuthenticationUser() {
        User user = new User();
        user.setUsername("javainuse");
        user.setPassword("javainuse");
        return user;
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
*/
