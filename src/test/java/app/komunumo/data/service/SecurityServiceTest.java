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
import app.komunumo.data.dto.UserRole;
import app.komunumo.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityServiceTest {

    @Test
    void getLoggedInUser() {
        final var userDto = new UserDto(null, null, null, "@test", "test@localhost",
                "Test User", "", null, UserRole.USER, "{noop}password");
        final var authenticatedUser = mock(AuthenticatedUser.class);
        when (authenticatedUser.getLoggedInUser()).thenReturn(Optional.of(userDto));
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);

        assertThat(testee.getLoggedInUser().orElseThrow()).isSameAs(userDto);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isUserLoggedInReturnsExpectedValue(final boolean expected) {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        when(authenticatedUser.isLoggedIn()).thenReturn(expected);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);

        assertThat(testee.isUserLoggedIn()).isEqualTo(expected);
    }

    @Test
    void loadUserByUsernameSuccess() {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        final var userDto = new UserDto(null, null, null, "@test", "test@localhost",
                "Test User", "", null, UserRole.USER, "{noop}password");
        when(userService.getUserByEmail("test@localhost")).thenReturn(Optional.of(userDto));

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);
        final var userDetails = testee.loadUserByUsername("test@localhost");
        final var authorities = userDetails.getAuthorities();

        assertThat(userDetails.getUsername()).isEqualTo("test@localhost");
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadAdminByUsernameSuccess() {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        final var userDto = new UserDto(null, null, null, "@test", "test@localhost",
                "Test User", "", null, UserRole.ADMIN, "{noop}password");
        when(userService.getUserByEmail("test@localhost")).thenReturn(Optional.of(userDto));

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);
        final var userDetails = testee.loadUserByUsername("test@localhost");
        final var authorities = userDetails.getAuthorities();

        assertThat(userDetails.getUsername()).isEqualTo("test@localhost");
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void loadUserByUsernameNotFound() {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        when(userService.getUserByEmail("test@localhost")).thenReturn(Optional.empty());

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);

        assertThatThrownBy(() -> testee.loadUserByUsername("test@localhost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("test@localhost");
    }

    @Test
    void generateRandomPassword() {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);
        final var randomPassword = testee.generateRandomPassword();

        assertThat(randomPassword)
                .hasSize(32)
                .doesNotContainAnyWhitespaces();
    }

    @Test
    void encodePassword() {
        final var authenticatedUser = mock(AuthenticatedUser.class);
        final var userService = mock(UserService.class);
        final var passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("password")).thenReturn("{noop}encodedPassword");

        final var testee = new SecurityService(authenticatedUser, userService, passwordEncoder);
        final var encodedPassword = testee.encodePassword("password");

        assertThat(encodedPassword).isEqualTo("{noop}encodedPassword");
    }

}
