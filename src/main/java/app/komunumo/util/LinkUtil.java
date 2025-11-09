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
package app.komunumo.util;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LinkUtil {

    private static @Nullable ConfigurationService configurationService;

    public static void initialize(final @Nullable ConfigurationService newConfigurationService) {
        configurationService = newConfigurationService;
    }

    private static @NotNull String addLinkPrefix(final @NotNull String link, final boolean withDomain) {
        final var prefix = withDomain && configurationService != null
                ? configurationService.getConfiguration(ConfigurationSetting.INSTANCE_URL)
                : "/";
        return prefix.concat(link);
    }

    public static @NotNull String getLink(final @NotNull EventDto event) {
        return getLink(event, false);
    }

    public static @NotNull String getLink(final @NotNull EventDto event, final boolean withDomain) {
        return addLinkPrefix("events/" + event.id(), withDomain);
    }

    public static @NotNull String getLink(final @NotNull CommunityDto community) {
        return getLink(community, false);
    }

    public static @NotNull String getLink(final @NotNull CommunityDto community, final boolean withDomain) {
        return addLinkPrefix("communities/" + community.profile(), withDomain);
    }

    private LinkUtil() {
        throw new IllegalStateException("Utility class");
    }

}
