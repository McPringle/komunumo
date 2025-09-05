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
package app.komunumo.ui.components;

import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.TranslationProvider;
import app.komunumo.ui.views.community.CommunityGridView;
import app.komunumo.ui.views.events.EventGridView;
import app.komunumo.ui.views.login.LoginDialog;
import app.komunumo.ui.views.login.LogoutView;
import app.komunumo.util.ThemeUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import org.jetbrains.annotations.NotNull;

public final class NavigationBar extends HorizontalLayout {

    public NavigationBar(final @NotNull ServiceProvider serviceProvider,
                         final @NotNull TranslationProvider translationProvider) {
        super();
        final var ui = UI.getCurrent();
        addClassName("navigation-bar");

        final var menuContainer = new Div();
        menuContainer.addClassName("menu-container");
        menuContainer.add(getNavigationBar(ui, serviceProvider));
        addToStart(menuContainer);

        addToEnd(getAvatar(ui, serviceProvider, translationProvider));
    }

    private Component getNavigationBar(final @NotNull UI ui,
                                       final @NotNull ServiceProvider serviceProvider) {
        final var menuBar = new Nav();
        menuBar.addClassName("menu-bar");
        menuBar.add(new RouterLink(ui.getTranslation("events.title"), EventGridView.class));
        if (!serviceProvider.getAppConfig().instance().hideCommunities()) {
            menuBar.add(new RouterLink(ui.getTranslation("communities.title"), CommunityGridView.class));
        }
        serviceProvider.globalPageService()
                .getGlobalPages(ui.getLocale())
                .forEach(page -> menuBar.add(new Anchor("/page/" + page.slot(), page.title())));
        return menuBar;
    }

    private Component getAvatar(final @NotNull UI ui,
                                final @NotNull ServiceProvider serviceProvider,
                                final @NotNull TranslationProvider translationProvider) {
        final var avatar = new Avatar();
        final var avatarMenu = new ContextMenu(avatar);
        avatarMenu.setOpenOnClick(true);

        // login as first entry in the menu
        if (!serviceProvider.loginService().isUserLoggedIn()) {
            avatarMenu.addItem(ui.getTranslation("login.title"), e ->
                    new LoginDialog(serviceProvider, translationProvider, getLocale()).open()
            );
        }

        // dark theme toggle
        avatarMenu.addItem(ui.getTranslation("avatar.menu.toggleDarkMode"), e -> ThemeUtil.toggleDarkMode());

        // logout as last entry in the menu
        if (serviceProvider.loginService().isUserLoggedIn()) {
            avatarMenu.addItem(ui.getTranslation("logout.title"), e ->
                    ui.navigate(LogoutView.class)
            );
        }

        return avatar;
    }

}
