
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
            CustomAuthenticationEntryPoint authenticationEntryPoint) {

        this.jwtFilter = jwtFilter;
        this.authenticationProvider = authenticationProvider;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/images/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/productos/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro"
                        ).hasAuthority("GERENTE")

                        // MI TRABAJO: SOLO TRABAJADOR
                        .requestMatchers(
                                "/api/v1/mi-trabajo",
                                "/api/v1/mi-trabajo/**"
                        ).hasAuthority("TRABAJADOR")

                        // ASIGNACIONES: CONSULTA
                        .requestMatchers(
                                HttpMethod.GET,
                                "/asignaciones",
                                "/asignaciones/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // ASIGNACIONES: MODIFICACIONES
                        .requestMatchers(
                                "/asignaciones",
                                "/asignaciones/**"
                        ).hasAuthority("GERENTE")

                        // ENTREGAS: CONSULTA
                        .requestMatchers(
                                HttpMethod.GET,
                                "/entregas",
                                "/entregas/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // ENTREGAS: MODIFICACIONES
                        .requestMatchers(
                                "/entregas",
                                "/entregas/**"
                        ).hasAuthority("GERENTE")

                        // TRABAJADORES Y VEHÍCULOS
                        .requestMatchers(
                                "/trabajadores",
                                "/trabajadores/**",
                                "/vehiculos",
                                "/vehiculos/**"
                        ).hasAuthority("GERENTE")

                        // CUENTAS
                        .requestMatchers(
                                "/usuarios",
                                "/usuarios/**",
                                "/api/v1/usuarios",
                                "/api/v1/usuarios/**"
                        ).hasAuthority("GERENTE")

                        // PEDIDOS: CONSULTA
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/pedidos/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // PEDIDOS: MODIFICACIONES
                        .requestMatchers(
                                "/api/v1/pedidos/**"
                        ).hasAuthority("GERENTE")

                        // PAGOS
                        .requestMatchers(
                                "/pagos",
                                "/pagos/**",
                                "/api/v1/pagos",
                                "/api/v1/pagos/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // REPORTES
                        .requestMatchers(
                                HttpMethod.GET,
                                "/reportes",
                                "/reportes/**",
                                "/api/v1/reportes",
                                "/api/v1/reportes/**"
                        ).hasAnyAuthority("GERENTE", "CONTADOR")

                        // PRODUCTOS: MODIFICACIONES
                        .requestMatchers(
                                "/api/v1/productos/**"
                        ).hasAuthority("GERENTE")

                        // RESTO DE API ADMINISTRATIVA
                        .requestMatchers(
                                "/api/v1/**"
                        ).hasAuthority("GERENTE")

                        // OTRAS RUTAS
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200",
                        "http://localhost:4201",
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
