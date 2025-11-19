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
package app.komunumo.business.event.boundary;

import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.event.control.EventService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@Route(value = "events", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class EventGridView extends AbstractView {

    public EventGridView(final @NotNull ConfigurationService configurationService,
                         final @NotNull EventService eventService) {
        super(configurationService);
        setId("events-view");
        final var events = eventService.getUpcomingEventsWithImage();
        add(new EventGrid(events));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "ui.views.events.EventGridView.title");
    }

}
