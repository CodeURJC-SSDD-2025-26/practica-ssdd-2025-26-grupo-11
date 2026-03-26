package es.codeurjc.practica2.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RepositoryUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
            .authorizeHttpRequests(authorize -> authorize
                // ---------------------
                // PUBLIC PAGES
                // ---------------------
                .requestMatchers(
                    "/", "/index",
                    "/login", "/loginerror",
                    "/register",
                    "/books", "/books/**",
                    "/book-detail/**",
                    "/css/**", "/js/**", "/images/**", "/image/**"
                ).permitAll()

                // ---------------------
                // PRIVATE PAGES FOR USER
                // ---------------------
                .requestMatchers(
                    "/base",
                    "/profile",
                    "/edit-profile",
                    "/my-loans"
                ).hasRole("USER")

                // ---------------------
                // PRIVATE PAGES FOR ADMIN
                // ---------------------
                .requestMatchers(
                    "/admin/admin-panel",
                    "/admin/admin-edit-book/**",
                    "/admin/admin-add-book",
                    "/admin/admin-edit-loan/**"
                ).hasRole("ADMIN")

                // ---------------------
                // ANY OTHER REQUEST NEEDS AUTH
                // ---------------------
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .failureUrl("/loginerror")
                .defaultSuccessUrl("/base")
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}