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
package app.komunumo.domain.core.layout.boundary;

import app.komunumo.domain.event.boundary.EventCard;
import app.komunumo.domain.event.boundary.MoreEventsCard;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.event.entity.EventWithImageDto;
import app.komunumo.util.ImageUtil;
import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@Route(value = "/home", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class HomeView extends VerticalLayout {

    public HomeView(final @NotNull EventService eventService) {
        super();

        final var pageId = "home-view";
        setId(pageId);
        addClassName(pageId);

        // Get last 3 past events with images
        final var pastEvents = eventService.getPastEventsWithImage();
        final var lastThreeEvents = pastEvents.stream()
        .limit(5)
        .toList();

        if (!lastThreeEvents.isEmpty()) {
            add(new H2(getPastEventsTitle()));
            this.addCarousel(pastEvents);
        } else {
            final var noEventsMessage = new Div();
            noEventsMessage.addClassName("no-events-message");
            noEventsMessage.getStyle()
                    .set("padding", "2rem")
                    .set("text-align", "center")
                    .set("font-size", "1.25rem")
                    .set("color", "var(--lumo-secondary-text-color)");
            noEventsMessage.setText(UI.getCurrent().getTranslation(UI.getCurrent().getLocale(),
                    "core.layout.boundary.HomeView.noPastEvents"));
            add(noEventsMessage);
        }

        add(new H2(getUpcommingEventsTitle()));

        var upcomingEvents = eventService.getUpcomingEventsWithImage().stream()
                .limit(3)
                .sorted((e1, e2) -> e1.event().begin().compareTo(e2.event().begin()))
                .toList();

        if (!upcomingEvents.isEmpty()) {
            final var upcomingGrid = new Div();
            upcomingGrid.addClassName("komunumo-grid");

            upcomingEvents.forEach(event -> {
                upcomingGrid.add(new EventCard(event));
            });

            upcomingGrid.add(new MoreEventsCard());

            add(upcomingGrid);
        } else {
            final var noEventsMessage = new Div();
            noEventsMessage.addClassName("no-events-message");
            noEventsMessage.getStyle()
                    .set("padding", "2rem")
                    .set("text-align", "center")
                    .set("font-size", "1.25rem")
                    .set("color", "var(--lumo-secondary-text-color)");
            noEventsMessage.setText(UI.getCurrent().getTranslation(UI.getCurrent().getLocale(),
                    "core.layout.boundary.HomeView.noUpcomingEvents"));
            add(noEventsMessage);
        }
    }

    protected @NotNull String getPastEventsTitle() {
        return UI.getCurrent().getTranslation(UI.getCurrent().getLocale(),
                            "community.boundary.CommunityDetailView.pastEvents");
    }

    protected @NotNull String getUpcommingEventsTitle() {
        return UI.getCurrent().getTranslation(UI.getCurrent().getLocale(),
                            "community.boundary.CommunityDetailView.upcomingEvents");
    }

    protected void addCarousel(final @NotNull Iterable<EventWithImageDto> pastEvents) {

        final var slides = new java.util.ArrayList<Slide>();

        pastEvents.forEach(event -> {
            final var eventTitle = event.event().title();
            final var eventImage = event.image() != null ? ImageUtil.resolveImageUrl(event.image())
                    : "/images/placeholder-400x225.svg";

            final var slideContent = new VerticalLayout();
            slideContent.setSpacing(false);
            slideContent.setPadding(false);
            slideContent.setAlignItems(Alignment.START);
            slideContent.getStyle()
                    .set("display", "flex")
                    .set("width", "100%")
                    .set("height", "100%");

            final var imageContainer = new Div();
            imageContainer.getStyle()
                .set("width", "100%")
                .set("flex", "1")
                .set("background-image", "url('" + eventImage + "')")
                .set("background-size", "cover").set("background-position", "center");

            final var title = new H3(eventTitle);
            title.getStyle().set("flex-shrink", "0");
            slideContent.add(imageContainer, title);
            slides.add(new Slide(slideContent));
        });

        final var carousel = new Carousel(slides.toArray(new Slide[0]))
                .withAutoProgress()
                .withSlideDuration(5);

        carousel.getStyle()
            .set("width", "min(100%, 1200px)")
            .set("margin", "0 auto");
        carousel.addClassName("past-events-carousel");

        add(carousel);
    }
}
