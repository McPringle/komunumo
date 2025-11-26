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
package app.komunumo.test;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface TestConstants {

    /**
     * UUID of the predefined local test user.
     */
    @NotNull UUID USER_ID_LOCAL = UUID.fromString("c9fc8b0a-6ff7-4c00-a6f2-d85f5829edff");

    /**
     * UUID of the predefined admin test user.
     */
    @NotNull UUID USER_ID_ADMIN = UUID.fromString("4a7c0871-915f-49d6-8182-9e1e8d253f79");

    /**
     * UUID of the predefined remote test user.
     */
    @NotNull UUID USER_ID_REMOTE = UUID.fromString("e7d67a8e-26f5-427d-9339-bf81fe598b39");

    /**
     * UUID of the predefined anonymous test user.
     */
    @NotNull UUID USER_ID_ANONYMOUS = UUID.fromString("2cf14b04-2975-4ac8-b68d-48e445180d26");

}
