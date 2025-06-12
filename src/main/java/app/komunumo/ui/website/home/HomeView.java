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

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.component.EventGrid;
import app.komunumo.ui.website.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_SLOGAN;

@Route(value = "", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class HomeView extends VerticalLayout implements HasDynamicTitle {

    private final transient @NotNull ConfigurationService configurationService;

    public HomeView(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.configurationService = serviceProvider.configurationService();
        setId("home-view");
        add(new EventGrid(serviceProvider.eventWithImageService().getUpcomingEventsWithImages()));
    }

    @Override
    public @NotNull String getPageTitle() {
        final var locale = UI.getCurrent().getLocale();
        final var instanceName = configurationService.getConfiguration(INSTANCE_NAME, locale);
        final var instanceSlogan = configurationService.getConfiguration(INSTANCE_SLOGAN, locale);
        return "%s – %s".formatted(instanceName, instanceSlogan);
    }

}
