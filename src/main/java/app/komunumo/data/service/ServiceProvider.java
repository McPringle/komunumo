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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public final class ServiceProvider {

    private final @NotNull AppConfig appConfig;
    private final @NotNull ConfigurationService configurationService;
    private final @NotNull SecurityService securityService;
    private final @NotNull CommunityService communityService;
    private final @NotNull EventService eventService;
    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull ImageService imageService;
    private final @NotNull EventWithImageService eventWithImageService;
    private final @NotNull UserService userService;
    private final @NotNull MailService mailService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ServiceProvider(final @NotNull AppConfig appConfig,
                           final @NotNull ConfigurationService configurationService,
                           final @NotNull SecurityService securityService,
                           final @NotNull CommunityService communityService,
                           final @NotNull GlobalPageService globalPageService,
                           final @NotNull EventService eventService,
                           final @NotNull ImageService imageService,
                           final @NotNull EventWithImageService eventWithImageService,
                           final @NotNull UserService userService,
                           final @NotNull MailService mailService) {
        super();
        this.appConfig = appConfig;
        this.configurationService = configurationService;
        this.securityService = securityService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.globalPageService = globalPageService;
        this.imageService = imageService;
        this.eventWithImageService = eventWithImageService;
        this.userService = userService;
        this.mailService = mailService;
    }

    public @NotNull AppConfig getAppConfig() {
        return appConfig;
    }

    public @NotNull ConfigurationService configurationService() {
        return configurationService;
    }

    public @NotNull SecurityService securityService() {
        return securityService;
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

    public @NotNull EventWithImageService eventWithImageService() {
        return eventWithImageService;
    }

    public @NotNull UserService userService() {
        return userService;
    }

    public @NotNull MailService mailService() {
        return mailService;
    }

}
