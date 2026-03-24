package com.irenewhub.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}


        //⚠️ Esto desactiva toda la seguridad temporalmente para que puedas probar los endpoints. Lo reemplazaremos completamente cuando lleguemos al paso de JWT.
        //Arranca la aplicación y dime qué ves en la consola. Si arranca sin errores, abre el navegador y ve a:
        //http://localhost:8080/api/products