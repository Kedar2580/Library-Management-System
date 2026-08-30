package com.library.config;

import com.library.security.CustomUserDetailsService;
import com.library.security.RoleAwareAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final RoleAwareAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          RoleAwareAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminLoginFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/admin-login", "/auth/admin-login/**")
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(sm -> sm.sessionConcurrency(sc -> sc
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry())))
                .formLogin(form -> form
                        .loginPage("/auth/admin-login")
                        .loginProcessingUrl("/auth/admin-login")
                        .successHandler(successHandler)
                        .permitAll())
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/auth/logout", "POST")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/auth/**", "/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/favicon.ico", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/books/new", "/books/*/edit", "/books/*/delete",
                                "/books/import", "/books/ratings/sync", "/categories/**", "/authors/**", "/publishers/**",
                                "/issues/**", "/returns/**", "/reservations/**", "/fines/**",
                                "/reports/**", "/members/**", "/admin/feedback", "/reviews/delete/**")
                        .hasAnyRole("ADMIN", "LIBRARIAN")
                        .anyRequest().authenticated())
                .sessionManagement(sm -> sm.sessionConcurrency(sc -> sc
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry())))
                .formLogin(form -> form
                        .loginPage("/auth/member-login")
                        .loginProcessingUrl("/auth/member-login")
                        .successHandler(successHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "POST"))
                        .logoutSuccessUrl("/")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, e) ->
                        response.sendRedirect("/auth/access-denied")))
                .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public static HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }
}
