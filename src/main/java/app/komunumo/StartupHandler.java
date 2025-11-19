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
package app.komunumo;

import app.komunumo.admin.AdminBootstrapper;
import app.komunumo.business.core.config.entity.AppConfig;
import app.komunumo.data.demo.DemoMode;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.user.control.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public final class StartupHandler {

    private final @NotNull AppConfig appConfig;
    private final @NotNull ConfigurationService configurationService;
    private final @NotNull UserService userService;
    private final @NotNull DemoMode demoMode;

    public StartupHandler(final @NotNull AppConfig appConfig,
                          final @NotNull ConfigurationService configurationService,
                          final @NotNull UserService userService,
                          final @NotNull DemoMode demoMode) {
        this.appConfig = appConfig;
        this.configurationService = configurationService;
        this.userService = userService;
        this.demoMode = demoMode;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        importRemoteData();
        clearCachedConfiguration();
        createInitialAdmin();
    }

    private void importRemoteData() {
        demoMode.resetDemoData();
    }

    private void clearCachedConfiguration() {
        configurationService.clearCache();
    }

    private void createInitialAdmin() {
        final var adminBootstrapper = new AdminBootstrapper(appConfig, userService);
        adminBootstrapper.createInitialAdminIfMissing();
    }

}
