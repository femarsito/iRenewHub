package com.irenewhub.backend.filter;

import com.irenewhub.backend.service.JwtService;
import com.irenewhub.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Leer el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay token o no empieza por "Bearer ", dejamos pasar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar "Bearer ")
        final String token = authHeader.substring(7);

        try {
            // 4. Extraer el email del token
            final String email = jwtService.extractEmail(token);

            // 5. Si hay email y no hay autenticación previa en el contexto
            if (email != null && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                // 6. Buscar el usuario en la base de datos
                userRepository.findByEmail(email).ifPresent(user -> {

                    // 7. Validar que el token es correcto
                    if (jwtService.isTokenValid(token, email)) {

                        // 8. Crear objeto de autenticación con el rol del usuario
                        var authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                        );

                        var authToken = new UsernamePasswordAuthenticationToken(
                                email, null, authorities
                        );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource()
                                        .buildDetails(request)
                        );

                        // 9. Establecer autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext()
                                .setAuthentication(authToken);
                    }
                });
            }
        } catch (Exception e) {
            // Token inválido o expirado — simplemente no autenticamos
            // No lanzamos excepción para no interrumpir el filtro
        }

        filterChain.doFilter(request, response);
    }
}