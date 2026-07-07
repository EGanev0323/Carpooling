package bg.tu_sofia.carpooling.config;

import bg.tu_sofia.carpooling.auth.service.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Validates the AUTH_TOKEN JWT cookie during the WebSocket handshake.
 * On success, stores the userId string in the WebSocket session attributes
 * so that the STOMP channel interceptor can set the Principal.
 */
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Cookie[] cookies = servletRequest.getServletRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                        try {
                            DecodedJWT jwt = jwtService.validateToken(cookie.getValue());
                            attributes.put("userId", jwt.getSubject());
                            return true;
                        } catch (Exception ex) {
                            log.debug("WebSocket handshake rejected — invalid JWT: {}", ex.getMessage());
                        }
                    }
                }
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // nothing to do post-handshake
    }
}
