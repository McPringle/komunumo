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
package app.komunumo.data.service;

import app.komunumo.configuration.AppConfig;
import app.komunumo.configuration.DemoConfig;
import app.komunumo.configuration.FilesConfig;
import app.komunumo.configuration.InstanceConfig;
import app.komunumo.configuration.MailConfig;
import app.komunumo.data.service.confirmation.ConfirmationService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ServiceProviderTest {

    @Test
    void testInstantiation() {
        final var demoConfig = new DemoConfig(false, "");
        final var filesConfig = new FilesConfig(Path.of(""));
        final var mailConfig = new MailConfig("test@example.com", "test@example.com");
        final var instanceConfig = new InstanceConfig("test@example.com", "", false);
        final var appConfig = new AppConfig("0.0.0", demoConfig, filesConfig, instanceConfig, mailConfig);

        final var confirmationService = mock(ConfirmationService.class);
        final var configurationService = mock(ConfigurationService.class);
        final var communityService = mock(CommunityService.class);
        final var globalPageService = mock(GlobalPageService.class);
        final var eventService = mock(EventService.class);
        final var imageService = mock(ImageService.class);
        final var userService = mock(UserService.class);
        final var mailService = mock(MailService.class);
        final var participationService = mock(ParticipationService.class);
        final var loginService = mock(LoginService.class);
        final var accountService = mock(AccountService.class);

        final var serviceProvider = new ServiceProvider(appConfig, configurationService, confirmationService,
                communityService, globalPageService, eventService, imageService, userService, mailService,
                participationService, loginService, accountService);

        assertThat(serviceProvider.getAppConfig()).isSameAs(appConfig);
        assertThat(serviceProvider.confirmationService()).isSameAs(confirmationService);
        assertThat(serviceProvider.configurationService()).isSameAs(configurationService);
        assertThat(serviceProvider.communityService()).isSameAs(communityService);
        assertThat(serviceProvider.globalPageService()).isSameAs(globalPageService);
        assertThat(serviceProvider.eventService()).isSameAs(eventService);
        assertThat(serviceProvider.imageService()).isSameAs(imageService);
        assertThat(serviceProvider.userService()).isSameAs(userService);
        assertThat(serviceProvider.mailService()).isSameAs(mailService);
        assertThat(serviceProvider.participationService()).isSameAs(participationService);
        assertThat(serviceProvider.loginService()).isSameAs(loginService);
        assertThat(serviceProvider.accountService()).isSameAs(accountService);
    }

}
