
package org.ecotrack_project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsServiceImpl usuarioDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // --- ENDPOINTS PÚBLICOS ---
                        .requestMatchers(
                                "/api/usuario/login",
                                "/api/usuario/registro",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // --- CRUD DE USUARIOS (SÓLO ADMINISTRADOR) ---
                        .requestMatchers(
                                "/api/usuario/total-paginas",
                                "/api/usuario/listar/**",
                                "/api/usuario/buscar/**",
                                "/api/usuario/crear-superusuario",
                                "/api/usuario/actualizar/**",
                                "/api/usuario/cambiar-estado/**"
                        ).hasRole("ADMINISTRADOR")

                        // --- GESTIÓN DE VEHÍCULOS (SÓLO ADMINISTRADOR) ---
                        .requestMatchers(
                                "/api/vehiculo/paginas",
                                "/api/vehiculo/listar",
                                "/api/vehiculo/buscar/**",
                                "/api/vehiculo/registrar",
                                "/api/vehiculo/actualizar/**",
                                "/api/vehiculo/cambiar-estado/**"
                        ).hasRole("ADMINISTRADOR")

                        // --- GESTIÓN DE MEDIOS DE PAGO (SÓLO ADMINISTRADOR) ---
                        .requestMatchers(
                                "/api/medio-pago/paginas",
                                "/api/medio-pago/listar-paginado",
                                "/api/medio-pago/buscar/**",
                                "/api/medio-pago/registrar",
                                "/api/medio-pago/actualizar/**",
                                "/api/medio-pago/cambiar-estado/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                "/api/carga/rendimiento/paginas",
                                "/api/carga/rendimiento/listar/**"
                        ).hasRole("ADMINISTRADOR")

                        // --- RESTO DE ENDPOINTS DEL SISTEMA ---
                        .requestMatchers(
                                "/api/programacion/mis-programaciones/*/cola/**",
                                "/api/programacion/mis-programaciones/*/finalizar"
                        ).hasAnyRole("JEFE_UNIDAD", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/programacion/mis-programaciones/**"
                        ).hasAnyRole("JEFE_UNIDAD", "ADMINISTRADOR", "TRANSPORTISTA")

                        .requestMatchers(
                                "/api/programacion/ejecutadas/**"
                        ).hasAnyRole("OPERARIO", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/cola/**"
                        ).hasAnyRole("OPERARIO", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/programacion/*/cola/pendientes"
                        ).hasAnyRole("OPERARIO", "JEFE_UNIDAD", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/carga/iniciar",
                                "/api/carga/salida/**",
                                "/api/carga/finalizar",
                                "/api/carga/registrar"
                        ).hasAnyRole("OPERARIO", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/carga/programacion/**"
                        ).hasAnyRole("OPERARIO", "JEFE_UNIDAD", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/reserva-unidad/**"
                        ).hasAnyRole("JEFE_UNIDAD", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/solicitud/pendientes/**",
                                "/api/solicitud/pie/**",
                                "/api/pago-inicial/pie/**",
                                "/api/pago-total/ptt/**",
                                "/api/solicitud/pi-confirmadas/**",
                                "/api/solicitud/pago-total/**",
                                "/api/programacion/**"
                        ).hasAnyRole("COORDINADOR", "ADMINISTRADOR")

                        .requestMatchers(
                                "/api/asignacion-inicial/buscar/**",
                                "/api/asignacion-total/buscar/**",
                                "/api/pago-inicial/pago/registrar/**",
                                "/api/pago-total/pago/registrar/**"
                        ).authenticated()

                        .requestMatchers("/api/contrato/**")
                        .authenticated()

                        .requestMatchers(
                                "/api/asignacion-inicial/registrar/**",
                                "/api/asignacion-inicial/validar/**",
                                "/api/asignacion-total/registrar/**",
                                "/api/asignacion-total/verificar/**"
                        ).hasAnyRole("COORDINADOR", "ADMINISTRADOR")

                        .requestMatchers("/api/medio-pago/**")
                        .authenticated()

                        .requestMatchers("/api/notificacion/**")
                        .authenticated()

                        .requestMatchers(
                                "/api/asignacion-coordinador/**"
                        ).hasRole("COORDINADOR")

                        .requestMatchers("/api/solicitud/**")
                        .authenticated()

                        .requestMatchers("/**")
                        .hasRole("ADMINISTRADOR")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
