
package pe.edu.upeu.pollogordo_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import pe.edu.upeu.pollogordo_api.exception.CustomAccessDeniedHandler;
import pe.edu.upeu.pollogordo_api.exception.CustomAuthenticationEntryPoint;
import pe.edu.upeu.pollogordo_api.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            AuthenticationProvider authenticationProvider,
            CustomAccessDeniedHandler accessDeniedHandler,
            CustomAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtFilter = jwtFilter;
        this.authenticationProvider = authenticationProvider;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Permitir conexión con Angular
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Desactivar CSRF porque utilizamos JWT
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // 1. Rutas públicas
                        .requestMatchers(
                                "/api/v1/auth/login"
                        ).permitAll()

                        // 2. Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 3. Registro de usuarios
                        // Solo el gerente puede registrar cuentas
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro"
                        ).hasAuthority("GERENTE")

                        // 4. Imágenes de productos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/images/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // 5. Consultar pedidos
                        // Gerente y contador
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/pedidos/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // 6. Crear, modificar y cancelar pedidos
                        // Solo gerente
                        .requestMatchers(
                                "/api/v1/pedidos/**"
                        ).hasAuthority("GERENTE")

                        // 7. Pagos
                        // Gerente y contador
                        .requestMatchers(
                                "/api/v1/pagos/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // 8. Otros módulos
                        // Productos, insumos, clientes,
                        // trabajadores, vehículos y asignaciones
                        .requestMatchers(
                                "/api/v1/**"
                        ).hasAuthority("GERENTE")

                        // 9. Otras solicitudes
                        .anyRequest().authenticated()
                )

                // No guardar sesiones
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Proveedor de autenticación
                .authenticationProvider(authenticationProvider)

                // Verificar JWT en cada solicitud
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Manejar errores de seguridad
                .exceptionHandling(exception -> exception

                        // Error 401: no autenticado
                        .authenticationEntryPoint(authenticationEntryPoint)

                        // Error 403: sin permisos
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Puertos permitidos de Angular
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://localhost:3080",
                        "http://localhost:4001"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
