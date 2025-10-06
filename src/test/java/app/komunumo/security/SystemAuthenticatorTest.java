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

import app.komunumo.data.dto.UserRole;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemAuthenticatorTest {

    private final @NotNull SystemAuthenticator authenticator = new SystemAuthenticator();

    @BeforeEach
    void setUp() {
        final Authentication alice = new UsernamePasswordAuthenticationToken(
                "alice", "secret",
                AuthorityUtils.createAuthorityList(UserRole.USER.getRole())
        );
        SecurityContextHolder.getContext().setAuthentication(alice);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void runAsAdmin_shouldSwitchRole_andRestorePrevious_afterCallable() {
        final var before = SecurityContextHolder.getContext().getAuthentication();
        assertThat(before).isNotNull();
        assertThat(authoritiesOf(before)).contains(UserRole.USER.getRole());

        final var result = authenticator.runAsAdmin(() -> {
            final var inside = SecurityContextHolder.getContext().getAuthentication();
            assertThat(inside).isNotNull();
            assertThat(inside.getName()).isEqualTo("system");
            assertThat(authoritiesOf(inside)).contains(UserRole.ADMIN.getRole());
            return 42;
        });

        assertThat(result).isEqualTo(42);

        final var after = SecurityContextHolder.getContext().getAuthentication();
        assertThat(after).isSameAs(before);
        assertThat(after.getName()).isEqualTo("alice");
        assertThat(authoritiesOf(after)).containsExactly(UserRole.USER.getRole());
    }

    @Test
    void runAsUser_runnable_shouldWork_withNullPreviousAuth_andRestoreToNull() {
        SecurityContextHolder.clearContext();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        final var capturedInside = new Authentication[1];

        authenticator.runAs(UserRole.USER, () -> {
            capturedInside[0] = SecurityContextHolder.getContext().getAuthentication();
            assertThat(capturedInside[0]).isNotNull();
            assertThat(capturedInside[0].getName()).isEqualTo("system");
            assertThat(authoritiesOf(capturedInside[0])).contains(UserRole.USER.getRole());
        });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void runAsAdmin_shouldWrapCheckedException_andRestorePrevious() {
        final var before = SecurityContextHolder.getContext().getAuthentication();

        assertThatThrownBy(() ->
                authenticator.runAsAdmin(() -> { throw new IOException("boom"); })
        )
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class)
                .hasMessageContaining("boom");

        final var after = SecurityContextHolder.getContext().getAuthentication();
        assertThat(after).isSameAs(before);
        assertThat(after.getName()).isEqualTo("alice");
        assertThat(authoritiesOf(after)).contains(UserRole.USER.getRole());
    }

    @Test
    void runAsUser_callable_shouldSwitchRole_andRestorePrevious() {
        final var before = new UsernamePasswordAuthenticationToken(
                "bob", "secret",
                AuthorityUtils.createAuthorityList(UserRole.ADMIN.getRole())
        );
        SecurityContextHolder.getContext().setAuthentication(before);

        final var result = authenticator.runAsUser(() -> {
            final var inside = SecurityContextHolder.getContext().getAuthentication();
            assertThat(inside).isNotNull();
            assertThat(inside.getName()).isEqualTo("system");
            assertThat(authoritiesOf(inside)).containsExactlyInAnyOrder(UserRole.USER.getRole());

            return 7;
        });

        assertThat(result).isEqualTo(7);

        final var after = SecurityContextHolder.getContext().getAuthentication();
        assertThat(after).isSameAs(before);
        assertThat(after.getName()).isEqualTo("bob");
        assertThat(authoritiesOf(after)).containsExactlyInAnyOrder(UserRole.ADMIN.getRole());
    }

    @Test
    void runAsUser_runnable_shouldWork_withNullPreviousAuth_andRestoreToNull_viaShortcut() {
        SecurityContextHolder.clearContext();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        final var captured = new Authentication[1];

        authenticator.runAsUser(() -> {
            captured[0] = SecurityContextHolder.getContext().getAuthentication();
            assertThat(captured[0]).isNotNull();
            assertThat(captured[0].getName()).isEqualTo("system");
            assertThat(authoritiesOf(captured[0])).containsExactlyInAnyOrder(UserRole.USER.getRole());
        });

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static @NotNull Set<String> authoritiesOf(final @NotNull Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

}
