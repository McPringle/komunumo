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
package app.komunumo.domain.user.control;

import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.test.KaribuTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserServiceKT extends KaribuTest {

    @Autowired
    private @NotNull UserService userService;

    @Test
    @SuppressWarnings("java:S5961")
    void happyCase() {
        // count original admins
        final var originalAdminCount = userService.getAdminCount();

        // create a new user
        var testUser = new UserDto(null, null, null,
                "@testUser", "test@example.eu", "Test User", "Test User Bio", null,
                UserRole.USER, UserType.LOCAL);
        testUser = userService.storeUser(testUser);
        final var testUserId = testUser.id();
        assertThat(testUserId).isNotNull().satisfies(testee -> {
            assertThat(testee.toString()).isNotEmpty();
            assertThat(testee.toString()).isNotBlank();
        });

        assertThat(testUser).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testUserId);
            assertThat(testee.profile()).isEqualTo("@testUser");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.name()).isEqualTo("Test User");
            assertThat(testee.bio()).isEqualTo("Test User Bio");
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.USER);
        });

        assertThat(userService.getAdminCount()).isEqualTo(originalAdminCount);

        // read user by email
        testUser = userService.getUserByEmail("test@example.eu").orElseThrow();
        assertThat(testUser).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testUserId);
            assertThat(testee.profile()).isEqualTo("@testUser");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.name()).isEqualTo("Test User");
            assertThat(testee.bio()).isEqualTo("Test User Bio");
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.USER);
        });

        // updating the existing user
        testUser = new UserDto(testUserId, testUser.created(), testUser.updated(), testUser.profile(), testUser.email(),
                "Test User Modified", testUser.bio(), testUser.imageId(), testUser.role(), testUser.type());
        testUser = userService.storeUser(testUser);
        assertThat(testUser).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testUserId);
            assertThat(testee.profile()).isEqualTo("@testUser");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isAfter(testee.created());
            assertThat(testee.name()).isEqualTo("Test User Modified");
            assertThat(testee.bio()).isEqualTo("Test User Bio");
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.USER);
        });

        assertThat(userService.getAdminCount()).isEqualTo(originalAdminCount);

        // create a new admin
        var testAdmin = new UserDto(null, null, null,
                "@testAdmin", "admin@example.eu", "Test Admin", "Test Admin Bio", null,
                UserRole.ADMIN, UserType.LOCAL);
        testAdmin = userService.storeUser(testAdmin);
        final var testAdminId = testAdmin.id();
        assertThat(testAdminId).isNotNull().satisfies(testee -> {
            assertThat(testee.toString()).isNotEmpty();
            assertThat(testee.toString()).isNotBlank();
        });

        assertThat(testAdmin).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testAdminId);
            assertThat(testee.profile()).isEqualTo("@testAdmin");
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.name()).isEqualTo("Test Admin");
            assertThat(testee.bio()).isEqualTo("Test Admin Bio");
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.ADMIN);
        });

        assertThat(userService.getAdminCount()).isEqualTo(originalAdminCount + 1);

        // delete the existing user
        assertThat(userService.deleteUser(testUser)).isTrue();
        assertThat(userService.deleteUser(testUser)).isFalse();
        assertThat(userService.getAdminCount()).isEqualTo(originalAdminCount + 1);

        // delete the existing admin
        assertThat(userService.deleteUser(testAdmin)).isTrue();
        assertThat(userService.deleteUser(testAdmin)).isFalse();
        assertThat(userService.getAdminCount()).isEqualTo(originalAdminCount);
    }

    @Test
    @SuppressWarnings("java:S5961")
    void createReadDeleteAnonymousUser() {
        final var email = "anonymous-%d@example.com".formatted(System.currentTimeMillis());
        assertThat(userService.getUserByEmail(email)).isEmpty();

        // create a new user
        var testUser = userService.createAnonymousUserWithEmail(email);
        final var testUserId = testUser.id();
        assertThat(testUserId).isNotNull().satisfies(testee -> {
            assertThat(testee.toString()).isNotEmpty();
            assertThat(testee.toString()).isNotBlank();
        });

        assertThat(testUser).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testUserId);
            assertThat(testee.profile()).isNull();
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.email()).isEqualTo(email);
            assertThat(testee.name()).isBlank();
            assertThat(testee.bio()).isBlank();
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.USER);
            assertThat(testee.type()).isEqualTo(UserType.ANONYMOUS);
        });

        // read user by email
        testUser = userService.getUserByEmail(email).orElseThrow();
        assertThat(testUser).isNotNull().satisfies(testee -> {
            assertThat(testee.id()).isEqualTo(testUserId);
            assertThat(testee.profile()).isNull();
            assertThat(testee.created()).isNotNull();
            assertThat(testee.updated()).isNotNull();
            assertThat(testee.updated()).isEqualTo(testee.created());
            assertThat(testee.email()).isEqualTo(email);
            assertThat(testee.name()).isBlank();
            assertThat(testee.bio()).isBlank();
            assertThat(testee.imageId()).isNull();
            assertThat(testee.role()).isEqualTo(UserRole.USER);
            assertThat(testee.type()).isEqualTo(UserType.ANONYMOUS);
        });

        // delete the existing user
        assertThat(userService.deleteUser(testUser)).isTrue();
        assertThat(userService.deleteUser(testUser)).isFalse();
        assertThat(userService.getUserByEmail(email)).isEmpty();
    }

    @Test
    void changeUserType_assertFailsWhenUserIdIsNull() {
        final var user = new UserDto(null, null, null, null, null, "", "", null,
                UserRole.USER, UserType.ANONYMOUS);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> userService.changeUserType(user, UserType.LOCAL))
                .withMessageContaining("User ID must not be null! Maybe the user is not stored yet?");
    }

}
