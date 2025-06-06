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

import app.komunumo.data.dto.GlobalPageDto;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.website.community.CommunityGridView;
import app.komunumo.ui.website.home.HomeView;
import app.komunumo.ui.website.login.LoginView;
import app.komunumo.ui.website.login.LogoutView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.router.RouterLink;
import org.jetbrains.annotations.NotNull;

public final class NavigationBar extends Nav {

    public NavigationBar(final @NotNull ServiceProvider serviceProvider) {
        super();
        final var ui = UI.getCurrent();
        final var locale = ui.getLocale();
        addClassName("navigation-bar");

        add(new RouterLink(ui.getTranslation("home.title"), HomeView.class));
        add(new RouterLink(ui.getTranslation("communities.title"), CommunityGridView.class));

        serviceProvider.globalPageService()
                .getGlobalPages(locale)
                .forEach(this::addGlobalPage);

        final var securityService = serviceProvider.securityService();
        if (securityService.isUserLoggedIn()) {
            add(new RouterLink(ui.getTranslation("logout.title"), LogoutView.class));
        } else {
            add(new RouterLink(ui.getTranslation("login.title"), LoginView.class));
        }
    }

    private void addGlobalPage(final @NotNull GlobalPageDto page) {
        add(new Anchor("/page/" + page.slot(), page.title()));
    }

}
