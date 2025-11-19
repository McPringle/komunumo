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
package app.komunumo.admin;

import app.komunumo.configuration.AppConfig;
import app.komunumo.business.user.entity.UserDto;
import app.komunumo.business.user.entity.UserRole;
import app.komunumo.business.user.entity.UserType;
import app.komunumo.business.user.control.UserService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class AdminBootstrapper {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapper.class);

    private final @NotNull String adminEmail;
    private final @NotNull UserService userService;

    public AdminBootstrapper(final @NotNull AppConfig appConfig,
                             final @NotNull UserService userService) {
        this.adminEmail = appConfig.instance().admin().trim().toLowerCase(Locale.getDefault());
        this.userService = userService;
    }

    public void createInitialAdminIfMissing() {
        if (userService.getAdminCount() > 0) {
            LOGGER.info("There are already instance admins. Skipping admin creation.");
            return;
        }

        if (adminEmail.isBlank()) {
            LOGGER.warn("No instance admin exists and KOMUNUMO_INSTANCE_ADMIN is not set. Skipping admin creation.");
            return;
        }

        final var adminUser = new UserDto(null, null, null,
                "@admin", adminEmail, "Admin", "", null,
                UserRole.ADMIN, UserType.LOCAL);
        userService.storeUser(adminUser);
        LOGGER.info("Initial admin user created with email: {}", adminEmail);
    }

}
