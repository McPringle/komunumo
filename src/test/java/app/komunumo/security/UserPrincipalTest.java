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

import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPrincipalTest {

    @Test
    void testUserWithAllValuesSet() {
        final var id = UUID.randomUUID();
        final var createdAt = ZonedDateTime.now();
        final var updatedAt = ZonedDateTime.now();
        final var profile = "@test@example.com";
        final var email = "test@example.com";
        final var name = "Test User";
        final var bio = "Just for testing purposes.";
        final var imageId = UUID.randomUUID();
        final var role = UserRole.USER;
        final var type = UserType.LOCAL;

        final var user = new UserDto(id, createdAt, updatedAt, profile, email, name, bio, imageId, role, type);
        final var authorities = List.of((GrantedAuthority) new SimpleGrantedAuthority("ROLE_USER"));

        final var principal = new UserPrincipal(user, authorities);

        assertThat(principal.getUserId().equals(id));
        assertThat(principal.getEmail().equals(email));
        assertThat(principal.getName().equals(name));
        assertThat(principal.getAuthorities()).isEqualTo(authorities);
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.getUsername().equals(email));
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void testUserWithNullId() {
        final var createdAt = ZonedDateTime.now();
        final var updatedAt = ZonedDateTime.now();
        final var profile = "@test@example.com";
        final var email = "test@example.com";
        final var name = "Test User";
        final var bio = "Just for testing purposes.";
        final var imageId = UUID.randomUUID();
        final var role = UserRole.USER;
        final var type = UserType.LOCAL;

        final var user = new UserDto(null, createdAt, updatedAt, profile, email, name, bio, imageId, role, type);
        final var authorities = List.of((GrantedAuthority) new SimpleGrantedAuthority("ROLE_USER"));

        assertThatThrownBy(() -> new UserPrincipal(user, authorities))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void testUserWithNullEmail() {
        final var id = UUID.randomUUID();
        final var createdAt = ZonedDateTime.now();
        final var updatedAt = ZonedDateTime.now();
        final var profile = "@test@example.com";
        final var name = "Test User";
        final var bio = "Just for testing purposes.";
        final var imageId = UUID.randomUUID();
        final var role = UserRole.USER;
        final var type = UserType.LOCAL;

        final var user = new UserDto(id, createdAt, updatedAt, profile, null, name, bio, imageId, role, type);
        final var authorities = List.of((GrantedAuthority) new SimpleGrantedAuthority("ROLE_USER"));

        assertThatThrownBy(() -> new UserPrincipal(user, authorities))
                .isInstanceOf(AssertionError.class);
    }

}
