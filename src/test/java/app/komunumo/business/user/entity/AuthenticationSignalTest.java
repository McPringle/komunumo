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
package app.komunumo.business.user.entity;

import app.komunumo.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationSignalTest {

    private AuthenticationSignal signal;

    @BeforeEach
    void setup() {
        signal = new AuthenticationSignal();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uninitialized_isNotAuthenticated_andNotAdmin() {
        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }


    @Test
    void setAuthenticated_false_resultsInNotAuthenticated_andNotAdmin() {
        signal.setAuthenticated(false);

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }


    @Test
    void setAuthenticated_true_resultsInAuthenticated_andNotAdmin() {
        signal.setAuthenticated(true);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void setAuthenticated_trueAndAdmin_resultsInAuthenticated_andAdmin() {
        signal.setAuthenticated(true, true, false);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isTrue();
        assertThat(signal.isLocalUser()).isFalse();
    }

    @Test
    void setAuthenticated_trueAndNotAdmin_resultsInAuthenticated_andNotAdmin() {
        signal.setAuthenticated(true, false, true);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
        assertThat(signal.isLocalUser()).isTrue();
    }

    @Test
    void setAuthenticated_falseAndAdmin_resultsInNotAuthenticated_andNotAdmin() {
        signal.setAuthenticated(false, true, false);

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
        assertThat(signal.isLocalUser()).isFalse();
    }

    @Test
    void setAuthenticated_trueAndLocalUser_resultsInAuthenticated_andLocalUser() {
        signal.setAuthenticated(true, false, false);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
        assertThat(signal.isLocalUser()).isFalse();
    }

    @Test
    void setAuthenticated_falseAndLocalUser_resultsInNotAuthenticated_andLocalUser() {
        signal.setAuthenticated(false, false, true);

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
        assertThat(signal.isLocalUser()).isFalse();
    }

    @Test
    void refreshFromSecurityContext_noAuthentication_setsFalseFalse() {
        signal.refreshFromSecurityContext();

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void refreshFromSecurityContext_nonUserPrincipalPrincipal_setsFalseFalse() {
        final var auth = new UsernamePasswordAuthenticationToken(
                "someone", "pw",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        signal.refreshFromSecurityContext();

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void refreshFromSecurityContext_authenticatedWithoutAdmin_setsTrueFalse() {
        final var principal = Mockito.mock(UserPrincipal.class);
        final var auth = new UsernamePasswordAuthenticationToken(
                principal, "pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        signal.refreshFromSecurityContext();

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void refreshFromSecurityContext_authenticatedWithAdmin_setsTrueTrue() {
        final var principal = Mockito.mock(UserPrincipal.class);
        final var auth = new UsernamePasswordAuthenticationToken(
                principal, "pw",
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        signal.refreshFromSecurityContext();

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isTrue();
    }

    @Test
    void refreshFromSecurityContext_anonymousUser_setsFalseFalse() {
        final var anonymousAuth = new AnonymousAuthenticationToken(
                "anonymousKey", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

        signal.refreshFromSecurityContext();

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }

}
