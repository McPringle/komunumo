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
package app.komunumo.ui.website.home;

import app.komunumo.ui.component.EventGrid;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.component.CommunityGrid;
import app.komunumo.ui.website.WebsiteLayout;

@Route(value = "", layout = WebsiteLayout.class)
@AnonymousAllowed
public class HomeView extends Div {

    public HomeView(final @NotNull ServiceProvider serviceProvider) {
        super();
        setId("home-view");

        final var ui = UI.getCurrent();
        final var tabSheet = new TabSheet();
        tabSheet.add(ui.getTranslation("communities.title"), new CommunityGrid(serviceProvider));
        tabSheet.add(ui.getTranslation("events.title"), new EventGrid(serviceProvider));
        add(tabSheet);
    }

}
