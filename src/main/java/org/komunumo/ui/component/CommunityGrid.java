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
package org.komunumo.ui.component;

import com.vaadin.flow.component.html.Div;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.DatabaseService;

public class CommunityGrid extends Div {

    public CommunityGrid(@NotNull final DatabaseService databaseService) {
        addClassName("community-grid");
        databaseService.getCommunities().forEach(community -> {
            final var image = databaseService.getImage(community.imageId()).orElse(null);
            final var card = new CommunityCard(community, image);
            add(card);
        });
    }
}
