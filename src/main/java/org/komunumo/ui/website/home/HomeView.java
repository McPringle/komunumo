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
package org.komunumo.ui.website.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.dto.CommunityDto;
import org.komunumo.data.service.DatabaseService;
import org.komunumo.ui.component.CommunityCard;
import org.komunumo.ui.website.WebsiteLayout;

@Route(value = "", layout = WebsiteLayout.class)
@AnonymousAllowed
public class HomeView extends Div {

    private final transient DatabaseService databaseService;

    public HomeView(@NotNull final DatabaseService databaseService) {
        this.databaseService = databaseService;

        setId("home-view");
        add(new H2("Home"));

        final var communityContainer = new HorizontalLayout();
        communityContainer.setWrap(true);
        databaseService.getCommunities()
                .map(this::createCommunityOverview)
                .forEach(communityContainer::add);
        add(communityContainer);
    }

    private Component createCommunityOverview(@NotNull final CommunityDto community) {
        final var image = databaseService.getImage(community.imageId()).orElse(null);
        return new CommunityCard(community, image);
    }

}
