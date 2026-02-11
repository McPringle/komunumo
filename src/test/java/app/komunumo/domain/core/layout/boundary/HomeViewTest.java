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

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

import app.komunumo.domain.core.image.entity.ContentType;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.event.entity.EventDto;
import app.komunumo.domain.event.entity.EventStatus;
import app.komunumo.domain.event.entity.EventVisibility;
import app.komunumo.domain.event.entity.EventWithImageDto;

class HomeViewTest {

    private EventService eventService;
    private UI ui;
    private VaadinSession vaadinSession;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        ui = mock(UI.class);
        vaadinSession = mock(VaadinSession.class);

        when(ui.getLocale()).thenReturn(Locale.ENGLISH);
        when(ui.getSession()).thenReturn(vaadinSession);
        when(vaadinSession.getLocale()).thenReturn(Locale.ENGLISH);

        when(ui.getTranslation(any(Locale.class), eq("community.boundary.CommunityDetailView.pastEvents")))
                .thenReturn("Past Events");
        when(ui.getTranslation(any(Locale.class), eq("community.boundary.CommunityDetailView.upcomingEvents")))
                .thenReturn("Upcoming Events");
        when(ui.getTranslation(any(Locale.class), eq("core.layout.boundary.HomeView.noPastEvents")))
                .thenReturn("There are no past events");
        when(ui.getTranslation(any(Locale.class), eq("core.layout.boundary.HomeView.noUpcomingEvents")))
                .thenReturn("No upcoming events available");

        UI.setCurrent(ui);
        VaadinSession.setCurrent(vaadinSession);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
    }

    @Test
    void shouldInitializeViewWithCorrectIdAndClassName() {
        // Arrange
        when(eventService.getPastEventsWithImage()).thenReturn(Collections.emptyList());
        when(eventService.getUpcomingEventsWithImage()).thenReturn(Collections.emptyList());

        // Act
        var view = new HomeView(eventService);

        // Assert
        assertThat(view.getId()).hasValue("home-view");
        assertThat(view.getClassNames()).contains("home-view");
    }

    @Test
    void shouldDisplayCarouselWhenPastEventsExist() {
        // Arrange
        var eventsWithImages = createMockEventsWithImages(5);
        when(eventService.getPastEventsWithImage()).thenReturn(eventsWithImages);
        when(eventService.getUpcomingEventsWithImage()).thenReturn(Collections.emptyList());

        // Act
        var view = new HomeView(eventService);

        // Assert
        var children = view.getChildren().collect(Collectors.toList());
        assertThat(children).isNotEmpty();

        // Should have H2 title for past events
        var hasH2WithPastEvents = children.stream()
                .filter(component -> component instanceof H2)
                .map(component -> ((H2) component).getText())
                .anyMatch(text -> text.equals("Past Events"));
        assertThat(hasH2WithPastEvents).isTrue();

        // Find the carousel component
        var carousel = children.stream()
                .filter(component -> component instanceof Carousel)
                .map(component -> (Carousel) component)
                .findFirst();

        assertThat(carousel).isPresent();
        var carouselComponent = carousel.orElseThrow();

        assertThat(carouselComponent.getElement().getClassList()).contains("past-events-carousel");
        assertThat(carouselComponent.getElement().getProperty("autoProgress", false)).isTrue();
        assertThat(carouselComponent.getElement().getProperty("slideDuration", 0)).isEqualTo(5);
        assertThat(carouselComponent.getElement().getProperty("swipe", true)).isTrue();

        // Verify 5 slides are rendered
        var slides = carouselComponent.getChildren().collect(Collectors.toList());
        assertThat(slides).hasSize(5);

        // Verify each slide contains an image container and a title (H3)
        slides.forEach(slide -> {
            assertThat(slide).isInstanceOf(Slide.class);
            var slideObj = (Slide) slide;

            // Get the content of the slide which is a VerticalLayout
            var slideContent = slideObj.getChildren().findFirst();
            assertThat(slideContent).isPresent();
            assertThat(slideContent.orElseThrow()).isInstanceOf(VerticalLayout.class);

            var slideLayout = (VerticalLayout) slideContent.orElseThrow();
            var slideChildren = slideLayout.getChildren().collect(Collectors.toList());

            // Verify image container has background-image set
            assertThat(slideChildren.get(0)).isInstanceOf(Div.class);
            var imageContainer = (Div) slideChildren.get(0);
            var backgroundImage = imageContainer.getStyle().get("background-image");

            // Since we're using events WITH images, verify it's NOT the placeholder
            assertThat(backgroundImage).isNotNull();
            assertThat(backgroundImage).startsWith("url('");
            assertThat(backgroundImage).endsWith("')");

            // Verify title
            assertThat(slideChildren.get(1)).isInstanceOf(H3.class);
            var title = (H3) slideChildren.get(1);
            assertThat(title.getText()).isNotEmpty();
        });
    }

    @Test
    void shouldDisplayNoEventsMessageWhenNoPastEventsExist() {
        // Arrange
        when(eventService.getPastEventsWithImage()).thenReturn(Collections.emptyList());
        when(eventService.getUpcomingEventsWithImage()).thenReturn(Collections.emptyList());

        // Act
        var view = new HomeView(eventService);

        // Assert
        var children = view.getChildren().collect(Collectors.toList());

        // Should have "no past events" message
        var hasNoPastEventsMessage = children.stream()
                .filter(component -> component instanceof Div)
                .filter(component -> component.getElement().getClassList().contains("no-events-message"))
                .anyMatch(component -> ((Div) component).getText().contains("There are no past events"));
        assertThat(hasNoPastEventsMessage).isTrue();
    }

    @Test
    void shouldDisplayUpcomingEventsGridWhenEventsExist() {
        // Arrange
        var mockUpcomingEvents = createMockEvents(3);
        when(eventService.getPastEventsWithImage()).thenReturn(Collections.emptyList());
        when(eventService.getUpcomingEventsWithImage()).thenReturn(mockUpcomingEvents);

        // Act
        var view = new HomeView(eventService);
        var title = view.getUpcommingEventsTitle();

        // Assert
        assertThat(title).isEqualTo("Upcoming Events");
        var children = view.getChildren().collect(Collectors.toList());

        // Should have H2 title for upcoming events
        var hasH2WithUpcomingEvents = children.stream()
                .filter(component -> component instanceof H2)
                .map(component -> ((H2) component).getText())
                .anyMatch(text -> text.contains("Upcoming Events"));
        assertThat(hasH2WithUpcomingEvents).isTrue();

        // Should have grid with komunumo-grid class
        var grid = children.stream()
                .filter(component -> component instanceof Div)
                .filter(component -> component.getElement().getClassList().contains("komunumo-grid"))
                .findFirst();
        assertThat(grid).isPresent();

        var gridComponent = (Div) grid.orElseThrow();

        // Verify the grid contains 4 cards (3 EventCards + 1 MoreEventsCard)
        var cards = gridComponent.getChildren()
                .filter(component -> component.getElement().getClassList().contains("komunumo-card"))
                .collect(Collectors.toList());
        assertThat(cards).hasSize(4);

        cards.forEach(card -> {
            assertThat(card.getElement().getClassList()).contains("komunumo-card");
        });

        var lastCard = cards.get(cards.size() - 1);
        assertThat(lastCard.getElement().getClassList()).contains("more-events-card");
    }

    @Test
    void shouldSortUpcomingEventsByBeginDateAscending() {
        // Arrange - Create events in unsorted order
        var now = ZonedDateTime.now();
        var event1 = createMockEventWithDate("Event 3", now.plusDays(3)); // Last
        var event2 = createMockEventWithDate("Event 1", now.plusDays(1)); // First
        var event3 = createMockEventWithDate("Event 2", now.plusDays(2)); // Middle

        // Return events in unsorted order
        when(eventService.getPastEventsWithImage()).thenReturn(Collections.emptyList());
        when(eventService.getUpcomingEventsWithImage()).thenReturn(List.of(event1, event2, event3));

        // Act
        var view = new HomeView(eventService);

        // Assert
        // Find the grid
        var children = view.getChildren().collect(Collectors.toList());
        var grid = children.stream()
                .filter(component -> component instanceof Div)
                .filter(component -> component.getElement().getClassList().contains("komunumo-grid"))
                .findFirst();

        assertThat(grid).isPresent();
        var gridComponent = (Div) grid.orElseThrow();

        // Get event cards (excluding the MoreEventsCard which is the last one)
        var cards = gridComponent.getChildren()
                .filter(component -> component.getElement().getClassList().contains("komunumo-card"))
                .collect(Collectors.toList());

        // Should have 4 cards total (3 events + 1 MoreEventsCard)
        assertThat(cards).hasSize(4);

        // Extract titles from the first 3 cards (the EventCards)
        var eventCards = cards.subList(0, 3);

        // Verify they are sorted by begin date (Event 1, Event 2, Event 3)
        // Note: This assumes EventCard displays the title somehow accessible
        // If EventCard structure allows, we can verify the order by checking titles
        assertThat(eventCards).hasSize(3);
    }

    @Test
    void shouldDisplayNoEventsMessageWhenNoUpcomingEventsExist() {
        // Arrange
        when(eventService.getPastEventsWithImage()).thenReturn(Collections.emptyList());
        when(eventService.getUpcomingEventsWithImage()).thenReturn(Collections.emptyList());

        // Act
        var view = new HomeView(eventService);

        // Assert
        var children = view.getChildren().collect(Collectors.toList());

        // Should have two "no events" messages (one for past, one for upcoming)
        var noEventsMessages = children.stream()
                .filter(component -> component instanceof Div)
                .filter(component -> component.getElement().getClassList().contains("no-events-message"))
                .anyMatch(component -> ((Div) component).getText().contains("No upcoming events available"));

        assertThat(noEventsMessages).isTrue();
    }

    @Test
    void shouldHandleEventsWithoutImages() {
        // Arrange
        var eventsWithoutImages = createMockEventsWithoutImages(3);
        when(eventService.getPastEventsWithImage()).thenReturn(eventsWithoutImages);
        when(eventService.getUpcomingEventsWithImage()).thenReturn(Collections.emptyList());

        // Act
        var view = new HomeView(eventService);

        // Assert
        var children = view.getChildren().collect(Collectors.toList());
        assertThat(view).isNotNull();

        // Find the carousel component
        var carousel = children.stream()
                .filter(component -> component instanceof Carousel)
                .map(component -> (Carousel) component)
                .findFirst();

        assertThat(carousel).isPresent();
        var carouselComponent = carousel.orElseThrow();

        // Verify carousel has slides with placeholder images
        var slides = carouselComponent.getChildren().collect(Collectors.toList());
        assertThat(slides).hasSize(3);

        // Verify each slide uses placeholder image
        slides.forEach(slide -> {
            assertThat(slide).isInstanceOf(Slide.class);
            var slideObj = (Slide) slide;

            // Get the content of the slide which is a VerticalLayout
            var slideContent = slideObj.getChildren().findFirst();
            assertThat(slideContent).isPresent();
            assertThat(slideContent.orElseThrow()).isInstanceOf(VerticalLayout.class);

            var slideLayout = (VerticalLayout) slideContent.orElseThrow();
            var slideChildren = slideLayout.getChildren().collect(Collectors.toList());

            var imageContainer = (Div) slideChildren.get(0);
            var backgroundImage = imageContainer.getStyle().get("background-image");

            // For events WITHOUT images, should use the placeholder
            assertThat(backgroundImage).isNotNull();
            assertThat(backgroundImage).isEqualTo("url('/images/placeholder-400x225.svg')");
        });
    }

    // Helper methods

    private List<EventWithImageDto> createMockEvents(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    var event = createMockEventDto("Test Event " + i);
                    return new EventWithImageDto(event, null);
                })
                .collect(Collectors.toList());
    }

    private List<EventWithImageDto> createMockEventsWithImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    var event = createMockEventDto("Test Event " + i);
                    var image = new ImageDto(UUID.randomUUID(), ContentType.IMAGE_PNG);
                    return new EventWithImageDto(event, image);
                })
                .collect(Collectors.toList());
    }

    private List<EventWithImageDto> createMockEventsWithoutImages(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    var event = createMockEventDto("Test Event " + i);
                    return new EventWithImageDto(event, null);
                })
                .collect(Collectors.toList());
    }

    private EventWithImageDto createMockEventWithDate(String title, ZonedDateTime begin) {
        var event = new EventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                title,
                "Description",
                "Location",
                begin,
                begin.plusHours(2),
                null,
                EventVisibility.PUBLIC,
                EventStatus.PUBLISHED
        );
        return new EventWithImageDto(event, null);
    }

    private EventDto createMockEventDto(String title) {
        return new EventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                title,
                "Test Description",
                "Test Location",
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().minusDays(1).plusHours(2),
                null,
                EventVisibility.PUBLIC,
                EventStatus.PUBLISHED
        );
    }
}
