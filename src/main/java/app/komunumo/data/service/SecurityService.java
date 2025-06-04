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
package app.komunumo.data.service;

import app.komunumo.data.dto.UserDto;
import app.komunumo.security.AuthenticatedUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class SecurityService implements UserDetailsService {

    private final @NotNull AuthenticatedUser authenticatedUser;
    private final @NotNull UserService userService;
    private final @NotNull PasswordEncoder passwordEncoder;

    public SecurityService(final @NotNull AuthenticatedUser authenticatedUser,
                           final @NotNull UserService userService,
                           final @NotNull PasswordEncoder passwordEncoder) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserDto> getLoggedInUser() {
        return authenticatedUser.getLoggedInUser();
    }

    public boolean isUserLoggedIn() {
        return authenticatedUser.isUserLoggedIn();
    }

    @Override
    public UserDetails loadUserByUsername(final @NotNull String email) throws UsernameNotFoundException {
        final var user = userService.getUserByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return mapToUserDetails(user);
    }

    private UserDetails mapToUserDetails(final @NotNull UserDto user) {
        final var roles = switch (user.role()) {
            case USER -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER"));
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"));
        };
        return new User(user.email(), user.passwordHash(), roles);
    }

    public String generateRandomPassword() {
        return RandomStringUtils.secureStrong().nextAscii(32).replaceAll("\\s", "_");
    }

    public String encodePassword(final String password) {
        return passwordEncoder.encode(password);
    }

}
