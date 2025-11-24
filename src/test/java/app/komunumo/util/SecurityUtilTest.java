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
package app.komunumo.util;

import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilTest {

    @Test
    void getUserPrincipal_returnsPrincipal() {
        final var principal = mock(UserPrincipal.class);
        final var authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            final var result = SecurityUtil.getUserPrincipal();
            assertThat(result).isPresent().contains(principal);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getUserPrincipal_authenticationIsNull() {
        SecurityContextHolder.clearContext(); // make sure it's empty
        final var result = SecurityUtil.getUserPrincipal();
        assertThat(result).isEmpty();
    }

    @Test
    void getUserPrincipal_notAuthenticated() {
        final var principal = mock(UserPrincipal.class);
        final var authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            final var result = SecurityUtil.getUserPrincipal();
            assertThat(result).isEmpty();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getUserPrincipal_principalIsNotUserPrincipal() {
        final var authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            final var result = SecurityUtil.getUserPrincipal();
            assertThat(result).isEmpty();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }


    @Test
    void isLoggedIn_returnsFalse() {
        SecurityContextHolder.clearContext();
        final boolean result = SecurityUtil.isLoggedIn();
        assertThat(result).isFalse();
    }

    @Test
    void isLoggedIn_returnsTrue() {
        final var principal = mock(UserPrincipal.class);
        final var authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            final boolean result = SecurityUtil.isLoggedIn();
            assertThat(result).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void hasRole_authenticationIsNull() {
        SecurityContextHolder.clearContext();
        final boolean result = SecurityUtil.hasRole(UserRole.ADMIN);
        assertThat(result).isFalse();
    }

    @Test
    void hasRole_notAuthenticated() {
        final var authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            final boolean result = SecurityUtil.hasRole(UserRole.ADMIN);
            assertThat(result).isFalse();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void hasRole_isAuthenticatedButRoleDoesNotMatch() {
        try {
            final var principal = mock(UserPrincipal.class);
            final var auth = new UsernamePasswordAuthenticationToken(
                    principal, "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            final boolean result = SecurityUtil.hasRole(UserRole.ADMIN);
            assertThat(result).isFalse();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void hasRole_isAuthenticatedAndRoleMatches() {
        try {
            final var principal = mock(UserPrincipal.class);
            final var auth = new UsernamePasswordAuthenticationToken(
                    principal, "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            final boolean result = SecurityUtil.hasRole(UserRole.ADMIN);
            assertThat(result).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void isAdmin_userDoesNotHaveAdminRole() {
        try {
            final var principal = mock(UserPrincipal.class);
            final var auth = new UsernamePasswordAuthenticationToken(
                    principal, "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            final boolean result = SecurityUtil.isAdmin();
            assertThat(result).isFalse();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void isAdmin_userHasAdminRole() {
        try {
            final var principal = mock(UserPrincipal.class);
            final var auth = new UsernamePasswordAuthenticationToken(
                    principal, "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            final boolean result = SecurityUtil.isAdmin();
            assertThat(result).isTrue();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
