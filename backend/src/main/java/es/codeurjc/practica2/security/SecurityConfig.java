package es.codeurjc.practica2.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import es.codeurjc.practica2.security.jwt.JwtRequestFilter;
import es.codeurjc.practica2.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private RepositoryUserDetailsService userDetailsService;

        @Autowired
        private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

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
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
                return new HiddenHttpMethodFilter();
        }

        // -------------------------------------------------------
        // @Order(1) — API REST filter chain
        // -------------------------------------------------------
        @Bean
        @Order(1)
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

                http.authenticationProvider(authenticationProvider());

                http
                                .securityMatcher("/api/**")
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint(unauthorizedHandlerJwt));

                http.authorizeHttpRequests(authorize -> authorize
                                // AUTH (IMPORTANTE)
                                .requestMatchers("/api/v1/auth/**").permitAll()

                                // PUBLIC
                                .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()

                                // ADMIN ONLY
                                .requestMatchers(HttpMethod.POST, "/api/v1/books/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole("ADMIN")

                                .anyRequest().authenticated());
                // Disable form login for API
                http.formLogin(formLogin -> formLogin.disable());

                // Disable CSRF for API
                http.csrf(csrf -> csrf.disable());

                // Stateless session for API
                http.sessionManagement(management -> management
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        // -------------------------------------------------------
        // @Order(2) — Web filter chain
        // -------------------------------------------------------
        @Bean
        @Order(2)
        public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

                http.authenticationProvider(authenticationProvider());

                http.authorizeHttpRequests(authorize -> authorize
                                // PUBLIC PAGES
                                .requestMatchers(
                                                "/",
                                                "/login", "/loginerror",
                                                "/register",
                                                "/books",
                                                "/book-detail/**",
                                                "/css/**", "/js/**", "/images/**", "/image/**",
                                                "/error/**")
                                .permitAll()
                                // USER PAGES
                                .requestMatchers(
                                                "/base",
                                                "/profile",
                                                "/edit-profile",
                                                "/my-loans")
                                .hasRole("USER")
                                // ADMIN PAGES
                                .requestMatchers(
                                                "/admin/admin-panel",
                                                "/admin/admin-edit-book/**",
                                                "/admin/admin-add-book",
                                                "/admin/edit-loans/**",
                                                "/admin/loan/**",
                                                "/admin/review/**",
                                                "/admin/user/**")
                                .hasRole("ADMIN")
                                .anyRequest().authenticated())

                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .failureUrl("/error/loginerror")
                                                .defaultSuccessUrl("/base")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }
}