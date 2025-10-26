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

import app.komunumo.data.service.AccountService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.LoginService;
import app.komunumo.ui.signals.AuthenticationSignal;
import app.komunumo.ui.views.admin.config.ConfigurationEditorView;
import app.komunumo.ui.views.community.CreateCommunityView;
import app.komunumo.ui.views.community.CommunityGridView;
import app.komunumo.ui.views.events.EventGridView;
import app.komunumo.ui.views.login.LogoutView;
import app.komunumo.util.LocationUtil;
import app.komunumo.util.ThemeUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEffect;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_REGISTRATION_ALLOWED;
import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_HIDE_COMMUNITIES;

public final class NavigationBar extends HorizontalLayout {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(NavigationBar.class);

    public NavigationBar(final @NotNull ConfigurationService configurationService,
                         final @NotNull GlobalPageService globalPageService,
                         final @NotNull LoginService loginService,
                         final @NotNull AccountService accountService,
                         final @NotNull AuthenticationSignal authenticationSignal) {
        super();
        final var ui = UI.getCurrent();
        addClassName("navigation-bar");

        final var menuContainer = new Div();
        menuContainer.addClassName("menu-container");
        menuContainer.add(getNavigationBar(ui, configurationService, globalPageService));
        addToStart(menuContainer);

        addToEnd(getAvatar(ui, configurationService, loginService, accountService, authenticationSignal));
    }

    private Component getNavigationBar(final @NotNull UI ui,
                                       final @NotNull ConfigurationService configurationService,
                                       final @NotNull GlobalPageService globalPageService) {
        final var menuBar = new Nav();
        menuBar.addClassName("menu-bar");
        menuBar.add(new RouterLink(ui.getTranslation("ui.components.NavigationBar.events"), EventGridView.class));

        if (!configurationService.getConfiguration(INSTANCE_HIDE_COMMUNITIES, Boolean.class)) {
            menuBar.add(new RouterLink(ui.getTranslation("ui.components.NavigationBar.communities"), CommunityGridView.class));
        }
        globalPageService
                .getGlobalPages(ui.getLocale())
                .forEach(page -> menuBar.add(new Anchor("/page/" + page.slot(), page.title())));
        return menuBar;
    }

    private Component getAvatar(final @NotNull UI ui,
                                final @NotNull ConfigurationService configurationService,
                                final @NotNull LoginService loginService,
                                final @NotNull AccountService accountService,
                                final @NotNull AuthenticationSignal authenticationSignal) {
        final var avatar = new Avatar();
        final var avatarMenu = new ContextMenu(avatar);
        avatarMenu.setOpenOnClick(true);

        // login as first entry in the menu
        final var loginItem = avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.login"), _ ->
                loginService.startLoginProcess(ui.getLocale(), LocationUtil.getCurrentLocation(ui))
        );

        final var registerItem = avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.register"), _ ->
                accountService.startRegistrationProcess(ui.getLocale(), LocationUtil.getCurrentLocation(ui))
        );

        // create community
        final var createCommunityItem = avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.createCommunity"),
                _ -> ui.navigate(CreateCommunityView.class));

        // admin menu
        final var configItem = avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.config"), _ ->
                ui.navigate(ConfigurationEditorView.class)
        );

        // dark theme toggle
        avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.toggleDarkMode"), _ -> ThemeUtil.toggleDarkMode());

        // logout as last entry in the menu
        final var logoutItem = avatarMenu.addItem(ui.getTranslation("ui.components.NavigationBar.logout"), _ ->
                ui.navigate(LogoutView.class)
        );

        // update menu items based on authentication state
        ComponentEffect.effect(this, () -> {
            final var registrationAllowed = configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class);
            LOGGER.warn("=== Registration allowed: {} ===", registrationAllowed);
            final var isLoggedIn = authenticationSignal.isAuthenticated();
            LOGGER.warn("=== User is logged in: {} ===", isLoggedIn);

            loginItem.setVisible(!isLoggedIn);
            registerItem.setVisible(registrationAllowed && !isLoggedIn);
            logoutItem.setVisible(isLoggedIn);

            final var isAdmin = authenticationSignal.isAdmin();
            configItem.setVisible(isAdmin);

            final var isLocalUser = authenticationSignal.isLocalUser();
            createCommunityItem.setVisible(isLocalUser);
        });

        return avatar;
    }

}
