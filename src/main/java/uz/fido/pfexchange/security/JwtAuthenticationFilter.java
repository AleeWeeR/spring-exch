package uz.fido.pfexchange.security;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final Logger _logger = LogManager.getLogger(JwtAuthenticationFilter.class);
    private final XmlMapper mapper = new XmlMapper();
    private final Gson gson;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username = jwtService.extractUsername(jwt);
                if (Objects.nonNull(username) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            _logger.error(e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ResponseWrapperDto<?> responseWrapperDto = ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Unauthorized")
                    .build();

            String contentType;

            try {
                contentType = request.getHeader("Accept");
            } catch (Exception ignored) {
                contentType = null;
            }

            String body;
            if (Objects.nonNull(contentType) && contentType.contains("application/xml")) {
                body = mapper.writeValueAsString(responseWrapperDto);
                response.setContentType("application/xml");
            } else {
                body = gson.toJson(responseWrapperDto);
                response.setContentType("application/json");
            }


            response.getWriter().write(body);
        }
    }
}
