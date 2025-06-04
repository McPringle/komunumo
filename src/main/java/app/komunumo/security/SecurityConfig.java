/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package app.komunumo.security;

import app.komunumo.ui.website.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

/**
 * <p>Security configuration for the Komunumo application.</p>
 *
 * <p>This class extends {@link VaadinWebSecurity} to integrate Spring Security
 * with Vaadin and define application-specific security rules.</p>
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    /**
     * The URL of the login page (relative to the application root).
     */
    public static final @NotNull String LOGIN_URL = "login";

    /**
     * The URL to redirect to after a successful logout.
     */
    public static final @NotNull String LOGOUT_SUCCESS_URL = "/";

    /**
     * <p>Configures HTTP security for the application.</p>
     *
     * <p>This method sets up default security rules and specifies
     * the custom login view to use.</p>
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception if an error occurs while configuring security
     */
    @Override
    protected void configure(final @NotNull HttpSecurity http) throws Exception {
        super.configure(http);
        setLoginView(http, LoginView.class, LOGOUT_SUCCESS_URL); // falls Logout-Redirect
    }

    /**
     * <p>Configures Spring Security to ignore specific request paths entirely,
     * bypassing the security filter chain. This is typically used for serving
     * static resources or custom servlets that do not require authentication.</p>
     *
     * @return a {@link WebSecurityCustomizer} that excludes specified paths from security
     */
    @Bean
    @Override
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/.well-known/**",
                        "/images/**");
    }

    /**
     * <p>Creates the password encoder used for storing and verifying passwords.</p>
     *
     * <p>This implementation uses BCrypt with a strength of 10 and a
     * {@link SecureRandom} instance to provide secure hashing.</p>
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10, new SecureRandom());
    }

}
