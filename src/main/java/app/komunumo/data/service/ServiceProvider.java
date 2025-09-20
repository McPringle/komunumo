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
import app.komunumo.data.service.confirmation.ConfirmationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("java:S6206")
public final class ServiceProvider {

    private final @NotNull AppConfig appConfig;
    private final @NotNull ConfigurationService configurationService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull CommunityService communityService;
    private final @NotNull EventService eventService;
    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull ImageService imageService;
    private final @NotNull UserService userService;
    private final @NotNull MailService mailService;
    private final @NotNull ParticipationService participationService;
    private final @NotNull LoginService loginService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ServiceProvider(final @NotNull AppConfig appConfig,
                           final @NotNull ConfigurationService configurationService,
                           final @NotNull ConfirmationService confirmationService,
                           final @NotNull CommunityService communityService,
                           final @NotNull GlobalPageService globalPageService,
                           final @NotNull EventService eventService,
                           final @NotNull ImageService imageService,
                           final @NotNull UserService userService,
                           final @NotNull MailService mailService,
                           final @NotNull ParticipationService participationService,
                           final @NotNull LoginService loginService) {
        super();
        this.appConfig = appConfig;
        this.configurationService = configurationService;
        this.confirmationService = confirmationService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.globalPageService = globalPageService;
        this.imageService = imageService;
        this.userService = userService;
        this.mailService = mailService;
        this.participationService = participationService;
        this.loginService = loginService;
    }

    public @NotNull AppConfig getAppConfig() {
        return appConfig;
    }

    public @NotNull ConfigurationService configurationService() {
        return configurationService;
    }

    public @NotNull ConfirmationService confirmationService() {
        return confirmationService;
    }

    public @NotNull CommunityService communityService() {
        return communityService;
    }

    public @NotNull GlobalPageService globalPageService() {
        return globalPageService;
    }

    public @NotNull ImageService imageService() {
        return imageService;
    }

    public @NotNull EventService eventService() {
        return eventService;
    }

    public @NotNull UserService userService() {
        return userService;
    }

    public @NotNull MailService mailService() {
        return mailService;
    }

    public @NotNull ParticipationService participationService() {
        return participationService;
    }

    public @NotNull LoginService loginService() {
        return loginService;
    }

}
