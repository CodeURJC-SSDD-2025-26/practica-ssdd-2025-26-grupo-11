package es.codeurjc.practica2.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.HiddenHttpMethodFilter;

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
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(authorize -> authorize
                        // ---------------------
                        // PUBLIC PAGES
                        // ---------------------
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/login", "/loginerror",
                                "/register",
                                "/books",
                                "/book-detail/**",
                                "/css/**", "/js/**", "/images/**", "/image/**",
                                "/error/**")
                        .permitAll()
                        // ---------------------
                        // PRIVATE PAGES FOR USER
                        // ---------------------
                        .requestMatchers(
                                "/base",
                                "/profile",
                                "/edit-profile",
                                "/my-loans")
                        .hasRole("USER")
                        // ---------------------
                        // PRIVATE PAGES FOR ADMIN
                        // ---------------------
                        .requestMatchers(
                                "/admin/admin-panel",
                                "/admin/admin-edit-book/**",
                                "/admin/admin-add-book",
                                "/admin/edit-loans/**",
                                "/admin/loan/**",
                                "/admin/review/**",
                                "/admin/user/**")
                        .hasRole("ADMIN")
                        // ---------------------
                        // ANY OTHER REQUEST NEEDS AUTH
                        // ---------------------
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/error/loginerror")
                        .defaultSuccessUrl("/base")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL for logout
                        .logoutSuccessUrl("/") // Redirects to homepage after logout
                        .invalidateHttpSession(true) // Removes the session
                        .deleteCookies("JSESSIONID") // Removes session cookie
                        .permitAll());

        return http.build();
    }
}
