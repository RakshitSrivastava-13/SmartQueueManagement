package com.hospital.queue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN", "STAFF")
                .build();

        UserDetails doctor = User.builder()
                .username("doctor1")
                .password(passwordEncoder.encode("doctor123"))
                .roles("DOCTOR", "STAFF")
                .build();

        UserDetails staff = User.builder()
                .username("staff1")
                .password(passwordEncoder.encode("staff123"))
                .roles("STAFF")
                .build();

        return new InMemoryUserDetailsManager(admin, doctor, staff);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Patient registration and queue viewing
                        .requestMatchers(HttpMethod.GET, "/departments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/queue/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tokens/number/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tokens/queue-position/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/patients").permitAll()
                        .requestMatchers(HttpMethod.POST, "/patients/find-or-register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/patients/phone/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tokens").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tokens/**").permitAll()
                        
                        // H2 Console
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // Staff endpoints - require authentication
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "DOCTOR", "STAFF")
                        
                        // Admin endpoints
                        .requestMatchers(HttpMethod.POST, "/departments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/departments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/departments/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 console

        return http.build();
    }
}
