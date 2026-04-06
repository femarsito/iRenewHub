package com.irenewhub.backend.config;

import com.irenewhub.backend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//le dice a Spring que no cree sesiones HTTP. Con JWT no las necesitamos, ya que se autentica sola con el token.
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /* CORS
    * Cuando Angular hace una petición a tu API, el navegador primero envía una petición OPTIONS
    * (llamada preflight) preguntando: "¿me permites hacer esto?". Tu backend responde con las
    * cabeceras CORS indicando qué está permitido, y solo entonces el navegador deja pasar la
    * petición real. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas — cualquiera puede acceder
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Rutas de admin — solo ADMIN puede crear/editar/borrar productos
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Rutas de usuario autenticado
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/contact/**").permitAll()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                //Coloca el filtro JWT antes del filtro de autenticacion por usuario/contraseña de Spring.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        //Solo localhost:4200 puede llamar a la API. En produccion se añadiria el nombre real
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        //Permite GET, POST, PUT, DELETE Y OPTIONS
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        //Acepta cualquier cabecera incluyendo Authorization, que necesitaremos para JWT
        config.setAllowedHeaders(List.of("*"));
        //Permite enviar cookies y cabeceras de autenticacion
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // api aplica esta configuracion solo a rutas de la API
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}