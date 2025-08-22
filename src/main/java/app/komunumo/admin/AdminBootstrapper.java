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
import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import app.komunumo.data.service.MailService;
import app.komunumo.data.service.SecurityService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.data.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public final class AdminBootstrapper {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(AdminBootstrapper.class);

    private final @NotNull String adminEmail;
    private final @NotNull UserService userService;
    private final @NotNull SecurityService securityService;
    private final @NotNull MailService mailService;

    public AdminBootstrapper(final @NotNull AppConfig appConfig,
                             final @NotNull ServiceProvider serviceProvider) {
        this.adminEmail = appConfig.instance().admin().trim().toLowerCase(Locale.getDefault());
        this.userService = serviceProvider.userService();
        this.securityService = serviceProvider.securityService();
        this.mailService = serviceProvider.mailService();
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

        final var password = securityService.generateRandomPassword();
        final var passwordHash = securityService.encodePassword(password);

        final var adminUser = new UserDto(null, null, null,
                "@admin", adminEmail, "Admin", "", null,
                UserRole.ADMIN, UserType.LOCAL, passwordHash);
        userService.storeUser(adminUser);

        mailService.sendMail(MailTemplateId.NEW_PASSWORD, Locale.getDefault(), MailFormat.MARKDOWN,
                Map.of("password", password), adminEmail);

        LOGGER.info("Initial admin user created with email: {}", adminEmail);
    }

}
