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
package app.komunumo.ui.views;

import app.komunumo.configuration.AppConfig;
import app.komunumo.business.user.control.RegistrationService;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.page.control.GlobalPageService;
import app.komunumo.business.user.control.LoginService;
import app.komunumo.ui.components.InfoBanner;
import app.komunumo.ui.components.NavigationBar;
import app.komunumo.ui.components.PageFooter;
import app.komunumo.ui.components.PageHeader;
import app.komunumo.ui.signals.AuthenticationSignal;
import app.komunumo.util.ThemeUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_SLOGAN;

public final class WebsiteLayout extends Div implements RouterLayout, BeforeEnterObserver {

    private final @NotNull Main main;

    public WebsiteLayout(final @NotNull AppConfig appConfig,
                         final @NotNull ConfigurationService configurationService,
                         final @NotNull GlobalPageService globalPageService,
                         final @NotNull LoginService loginService,
                         final @NotNull RegistrationService registrationService,
                         final @NotNull AuthenticationSignal authenticationSignal) {
        super();
        authenticationSignal.refreshFromSecurityContext();
        final var ui = UI.getCurrent();

        if (appConfig.demo().enabled()) {
            add(new InfoBanner(ui.getTranslation("ui.views.WebsiteLayout.demoMode")));
        }

        addPageHeader(configurationService);
        add(new NavigationBar(configurationService, globalPageService, loginService, registrationService, authenticationSignal));

        main = new Main();
        add(main);

        final var komunumoVersion = appConfig.version();
        add(new PageFooter(ui, komunumoVersion));
    }

    private void addPageHeader(final @NotNull ConfigurationService configurationService) {
        final var locale = UI.getCurrent().getLocale();
        final var instanceTitle = configurationService.getConfiguration(INSTANCE_NAME);
        final var instanceSlogan = configurationService.getConfiguration(INSTANCE_SLOGAN, locale);
        add(new PageHeader(instanceTitle, instanceSlogan));
    }

    @Override
    public void showRouterLayoutContent(final @NotNull HasElement content) {
        main.removeAll();
        main.add(content.getElement().getComponent()
                .orElseThrow(() -> new IllegalArgumentException(
                        "WebsiteLayout content must be a Component")));
    }

    @Override
    public void removeRouterLayoutContent(final @Nullable HasElement oldContent) {
        main.removeAll();
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent event) {
        ThemeUtil.initializeDarkMode();
    }

}
