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
package app.komunumo.ui;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface TestConstants {

    /**
     * UUID of the predefined local test user.
     */
    @NotNull UUID USER_ID_LOCAL = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /**
     * UUID of the predefined admin test user.
     */
    @NotNull UUID USER_ID_ADMIN = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /**
     * UUID of the predefined remote test user.
     */
    @NotNull UUID USER_ID_REMOTE = UUID.fromString("33333333-3333-3333-3333-333333333333");

    /**
     * UUID of the predefined anonymous test user.
     */
    @NotNull UUID USER_ID_ANONYMOUS = UUID.fromString("44444444-4444-4444-4444-444444444444");

}
