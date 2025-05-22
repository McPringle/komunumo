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
package app.komunumo.ui.component;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;

public class EventGrid extends KomunumoGrid {

    public EventGrid(final @NotNull ServiceProvider serviceProvider) {
        super(serviceProvider.communityService()
                .getCommunities()
                .map(community -> mapToCard(serviceProvider, community))
                .toArray(EventCard[]::new));
        addClassName("community-grid");
    }

    private static @NotNull EventCard mapToCard(final @NotNull ServiceProvider serviceProvider,
                                                   final @NotNull CommunityDto community) {
        final var image = serviceProvider.imageService()
                .getImage(community.imageId())
                .orElse(null);
        return new EventCard(community, image);
    }
}
