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
package app.komunumo.ui.website.events;

import app.komunumo.data.dto.EventWithImageDto;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.component.AbstractView;
import app.komunumo.ui.website.WebsiteLayout;
import app.komunumo.util.DateTimeUtil;
import app.komunumo.util.ImageUtil;
import app.komunumo.util.ParseUtil;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Locale;

@Route(value = "events/:eventId", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class EventDetailView extends AbstractView implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDetailView.class);

    private final transient @NotNull EventService eventService;

    private final @NotNull HtmlContainer pageContent = new Div();

    private @NotNull String pageTitle = "";

    public EventDetailView(final @NotNull ServiceProvider serviceProvider) {
        super(serviceProvider.configurationService());
        this.eventService = serviceProvider.eventService();
        addClassName("event-detail-view");
        add(pageContent);
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var eventId = ParseUtil.parseUUID(params.get("eventId"))
                .orElseThrow(() -> new NotFoundException("Invalid event ID!"));
        final var ui = beforeEnterEvent.getUI();
        final var locale = ui.getLocale();

        eventService.getEventWithImage(eventId).ifPresentOrElse(eventWithImage -> {
            showDetails(eventWithImage, locale);
            pageTitle = eventWithImage.event().title();
        }, () -> {
            LOGGER.warn("Event not found with id '{}'!", eventId);
            beforeEnterEvent.rerouteToError(NotFoundException.class);
        });
    }

    private void showDetails(final @NotNull EventWithImageDto eventWithImage,
                             final @NotNull Locale locale) {
        pageContent.removeAll();

        final var event = eventWithImage.event();
        final var image = eventWithImage.image();

        if (image != null) {
            final var imageUrl = ImageUtil.resolveImageUrl(image);
            final var altText = getTranslation(locale, "event.details.image.altText", event.title());
            final var htmlImage = new Image(imageUrl, altText);
            htmlImage.addClassName("event-image");
            pageContent.add(htmlImage);
        }

        final var title = new H2(event.title());
        title.addClassName("event-title");
        pageContent.add(title);

        final var location = new Paragraph(
                getTranslation(locale, "event.details.location") + ": " + event.location());
        location.addClassName("event-location");
        pageContent.add(location);

        addDateTimeText(event.begin(), locale, "event.details.beginDate", "event-date-begin");
        addDateTimeText(event.end(), locale, "event.details.endDate", "event-date-end");

        final var description = new Markdown(event.description());
        description.addClassName("event-description");
        pageContent.add(description);
    }

    private void addDateTimeText(final @Nullable ZonedDateTime dateTime,
                                 final @NotNull Locale locale,
                                 final @NotNull String translationKey,
                                 final @NotNull String className) {
        final var label = getTranslation(locale, translationKey);
        final var localizedEndDate = DateTimeUtil.getLocalizedDateTimeString(dateTime, locale);
        final var paragraph = new Paragraph(label + ": " + localizedEndDate);
        paragraph.addClassName(className);
        pageContent.add(paragraph);
    }

    @Override
    protected @NotNull String getViewTitle() {
        return pageTitle;
    }

}
