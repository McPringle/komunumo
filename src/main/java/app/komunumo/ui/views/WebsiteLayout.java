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

import app.komunumo.data.service.ServiceProvider;
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

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_SLOGAN;

public final class WebsiteLayout extends Div implements RouterLayout, BeforeEnterObserver {

    private final @NotNull UI ui;
    private final @NotNull Main main;

    public WebsiteLayout(final @NotNull ServiceProvider serviceProvider,
                         final @NotNull AuthenticationSignal authenticationSignal) {
        super();
        authenticationSignal.refreshFromSecurityContext();
        ui = UI.getCurrent();

        if (serviceProvider.getAppConfig().demo().enabled()) {
            add(new InfoBanner(ui.getTranslation("ui.views.WebsiteLayout.demoMode")));
        }

        addPageHeader(serviceProvider);
        addNavigationBar(serviceProvider, authenticationSignal);

        main = new Main();
        add(main);

        addFooter(serviceProvider);
    }

    private void addPageHeader(final @NotNull ServiceProvider serviceProvider) {
        final var locale = UI.getCurrent().getLocale();
        final var configurationService = serviceProvider.configurationService();
        final var instanceTitle = configurationService.getConfiguration(INSTANCE_NAME);
        final var instanceSlogan = configurationService.getConfiguration(INSTANCE_SLOGAN, locale);
        add(new PageHeader(instanceTitle, instanceSlogan));
    }

    private void addNavigationBar(final @NotNull ServiceProvider serviceProvider,
                                  final @NotNull AuthenticationSignal authenticationSignal) {
        add(new NavigationBar(serviceProvider, authenticationSignal));
    }

    private void addFooter(final @NotNull ServiceProvider serviceProvider) {
        final var komunumoVersion = serviceProvider.getAppConfig().version();
        add(new PageFooter(ui, komunumoVersion));
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
