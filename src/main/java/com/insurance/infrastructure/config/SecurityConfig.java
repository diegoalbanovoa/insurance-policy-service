package com.insurance.infrastructure.config;

import com.insurance.infrastructure.security.JwtAuthenticationFilter;
import com.insurance.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.cors.allowed-origins}")
    private String corsOrigins;

    @Value("${app.cors.allowed-methods}")
    private String corsMethods;

    @Value("${app.cors.allowed-headers}")
    private String corsHeaders;

    @Value("${app.cors.max-age}")
    private long corsMaxAge;

    /**
     * Bean para codificar contraseñas usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para obtener el gestor de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Filtra JWT del request y lo valida
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /**
     * Configuración principal de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Habilitar CORS basado en configuración
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilitar CSRF (sin estado con JWT)
                .csrf(csrf -> csrf.disable())
                
                // Sin sesiones (JWT es stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configuración de autorización
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        
                        // Endpoints de lectura permitida
                        .requestMatchers(HttpMethod.GET, "/clients/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/policies/**").permitAll()
                        
                        // Todos los demás requieren autenticación
                        .anyRequest().authenticated()
                )
                
                // Manejo de excepciones
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + 
                                    authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + 
                                    accessDeniedException.getMessage() + "\"}");
                        })
                );

        // Agregar filtro JWT antes del filtro de autenticación estándar
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS desde variables de entorno
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Procesar origins desde lista separada por coma
        String[] allowedOrigins = corsOrigins.split(",");
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        
        // Procesar métodos permitidos
        String[] allowedMethods = corsMethods.split(",");
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        
        // Procesar headers permitidos
        String[] allowedHeaders = corsHeaders.split(",");
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        
        configuration.setMaxAge(corsMaxAge);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
