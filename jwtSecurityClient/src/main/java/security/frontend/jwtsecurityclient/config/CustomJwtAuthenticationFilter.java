package security.frontend.jwtsecurityclient.config;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers;
import io.jsonwebtoken.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Filter for ensure that jwt token is valid
 * That filter will be executed only once per request
 * Check if the token is expired
 */
@Component
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            // JWT Token is in the form "Bearer token". Remove Bearer word and
            // get  only the Token
            String jwtAccessToken = extractAccessTokenFromRequest(request);
            String jwtRefreshToken = extractRefreshTokenFromRequest(request);

            if (StringUtils.hasText(jwtRefreshToken) && validateToken(jwtRefreshToken)) {

                //if the access token is null (expired), but the refresh token is valid, then we need to generate new access token
                if (jwtAccessToken == null) {
                    request.setAttribute("isRefreshNeeded", true);
                    request.setAttribute("refreshToken", jwtRefreshToken);

                    //allow the refresh of the access token
                    UserDetails userDetails = new User(getUsernameFromToken(jwtRefreshToken), "",
                            getRolesFromToken(jwtRefreshToken));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
                    UserDetails userDetails = new User(getUsernameFromToken(jwtAccessToken), "",
                            getRolesFromToken(jwtAccessToken));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                }
            } else if (jwtRefreshToken != null && validateToken(jwtRefreshToken)) {
                request.setAttribute("exception", new ExpiredJwtException(null, null, "Refresh token is expired"));
            } else {
                System.out.println("Cannot set the Security Context");
            }
        } catch (BadCredentialsException ex) {
            request.setAttribute("exception", ex);
        }
        chain.doFilter(request, response);
    }

    private String extractAccessTokenFromRequest(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    private String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    private List<SimpleGrantedAuthority> getRolesFromToken(String token) {
        List<SimpleGrantedAuthority> roles = null;
        Claims claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(token).getBody();
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isUser = claims.get("isUser", Boolean.class);
        if (isAdmin != null && isAdmin)
            roles = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isUser != null && isUser)
            roles = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return roles;
    }

    private boolean validateToken(String authToken) throws BadCredentialsException, ExpiredJwtException {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey("secret").parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            return false;
        }
    }
}
