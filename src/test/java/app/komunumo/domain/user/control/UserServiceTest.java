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
import app.komunumo.infra.persistence.jooq.UniqueIdGenerator;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    @Test
    void isProfileComplete() {
        final var completeProfile = new UserDto(null, null, null,
                "demoUserComplete", "demo-user-complete@example.com", "Demo User", "", null,
                UserRole.USER, UserType.LOCAL);
        final var incompleteProfile = new UserDto(null, null, null,
                "demoUserIncomplete", "demo-user-incomplete@example.com", " ", "", null,
                UserRole.USER, UserType.LOCAL);

        final var userService = new UserService(mock(DSLContext.class), mock(UniqueIdGenerator.class));
        assertThat(userService.isProfileComplete(completeProfile)).isTrue();
        assertThat(userService.isProfileComplete(incompleteProfile)).isFalse();
    }

}
