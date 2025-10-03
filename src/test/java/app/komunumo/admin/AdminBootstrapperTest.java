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
import app.komunumo.configuration.DemoConfig;
import app.komunumo.configuration.FilesConfig;
import app.komunumo.configuration.InstanceConfig;
import app.komunumo.configuration.MailConfig;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.service.MailService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.data.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapperTest {

    @Test
    @SuppressWarnings("DataFlowIssue")
    void shouldCreateAdminIfNoneExistsAndEmailIsSet() {
        final var serviceProvider = mockServiceProvider(0);
        final var appConfig = createAppConfig("admin@example.eu");

        final var bootstrapper = new AdminBootstrapper(appConfig, serviceProvider);
        bootstrapper.createInitialAdminIfMissing();

        verify(serviceProvider.userService()).storeUser(argThat(user ->
                user.email().equals("admin@example.eu") &&
                        user.role() == UserRole.ADMIN &&
                        user.profile().equals("@admin")
        ));
    }

    @Test
    void shouldSkipCreationIfAdminAlreadyExists() {
        final var serviceProvider = mockServiceProvider(1);
        final var appConfig = createAppConfig("admin@example.eu");

        final var bootstrapper = new AdminBootstrapper(appConfig, serviceProvider);
        bootstrapper.createInitialAdminIfMissing();

        verify(serviceProvider.userService(), never()).storeUser(any());
    }

    @Test
    void shouldSkipCreationIfNoEmailSet() {
        final var serviceProvider = mockServiceProvider(0);
        final var appConfig = createAppConfig("");

        final var bootstrapper = new AdminBootstrapper(appConfig, serviceProvider);
        bootstrapper.createInitialAdminIfMissing();

        verify(serviceProvider.userService(), never()).storeUser(any());
    }

    private ServiceProvider mockServiceProvider(final int adminCount) {
        final var userService = mock(UserService.class);
        when(userService.getAdminCount()).thenReturn(adminCount);
        final var mailService = mock(MailService.class);
        when(mailService.sendMail(any(), any(), any(), any(), any())).thenReturn(true);
        final var serviceProvider = mock(ServiceProvider.class);
        when(serviceProvider.userService()).thenReturn(userService);
        when(serviceProvider.mailService()).thenReturn(mailService);
        return serviceProvider;
    }

    private AppConfig createAppConfig(final @NotNull String email) {
        final var version = "0.0.0";
        final var demoConfig = new DemoConfig(false, "");
        final var filesConfig = new FilesConfig(Path.of("/tmp"));
        final var instanceConfig = new InstanceConfig(email);
        final var mailConfig = new MailConfig("", "");
        return new AppConfig(version, demoConfig, filesConfig, instanceConfig, mailConfig);
    }

}
