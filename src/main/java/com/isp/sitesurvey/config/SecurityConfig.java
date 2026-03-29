package com.isp.sitesurvey.config;

import com.isp.sitesurvey.security.JwtAuthenticationEntryPoint;
import com.isp.sitesurvey.security.JwtAuthenticationFilter;
import com.isp.sitesurvey.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Main Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Disable CSRF (JWT + REST API)
            .csrf(AbstractHttpConfigurer::disable)

            // Exception handling
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // Stateless session (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // -------------------------------------------------------------
                // PUBLIC ENDPOINTS (No Token Required)
                // -------------------------------------------------------------
                .requestMatchers(
                    "/api/auth/**",
                    "/api/files/**",           // Allows floor plan files
                    "/error",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/engineer/**"         // Bypasses 403 error for Jane
                ).permitAll()

                // Allow public VIEWING of images (GET), but keep uploads locked down
                .requestMatchers(HttpMethod.GET, "/api/properties/*/image").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/properties/extra-images/*").permitAll()

                // Allow GET access to checklists so the React panel can load templates
                .requestMatchers(HttpMethod.GET, "/api/checklists", "/api/checklists/**").permitAll()

                // -------------------------------------------------------------
                // AUTHENTICATED ENDPOINTS (Requires Valid Token)
                // -------------------------------------------------------------
                
                // Explicitly permit PATCH and PUT on checklist responses (Needed for CORS)
                .requestMatchers(HttpMethod.PATCH, "/api/checklists/responses/**").authenticated()
                .requestMatchers(HttpMethod.PUT,   "/api/checklists/responses/**").authenticated()

                // RF Tools 
                .requestMatchers("/api/rf/**").authenticated()

                // -------------------------------------------------------------
                // ROLE-BASED ENDPOINTS (Requires Specific Roles)
                // -------------------------------------------------------------
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/client/**").hasRole("CLIENT")
                .requestMatchers("/api/account/**").hasRole("ACCOUNT_MANAGER")
                .requestMatchers("/api/finance/**").hasRole("FINANCE_MANAGER")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ACCOUNT_MANAGER", "ONSITE_ENGINEER")
                
                // FIXED: Reports are strictly for Admins and Account Managers
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ACCOUNT_MANAGER")

                // Everything else not explicitly mentioned requires authentication
                .anyRequest().authenticated()
            )

            // Authentication provider
            .authenticationProvider(authenticationProvider())

            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration (Vite frontend)
     * Includes GET, POST, PUT, DELETE, PATCH, OPTIONS to prevent CORS block on checklists
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vite frontend default
            "http://localhost:5174",  // Alternate Vite frontend port
            "http://localhost:3000"   // Standard React port
        ));

        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}