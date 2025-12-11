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
package app.komunumo.domain.community.boundary;

import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.community.entity.CommunityDto;
import app.komunumo.domain.community.entity.CommunityWithImageDto;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.domain.event.boundary.CreateEventView;
import app.komunumo.domain.event.boundary.EventGrid;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.util.ImageUtil;
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Route(value = "communities/:profile", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class CommunityDetailView extends AbstractView implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityDetailView.class);

    private final transient @NotNull CommunityService communityService;

    private final @NotNull HtmlContainer pageContent = new Div();
    private final @NotNull MemberService memberService;
    private final @NotNull EventService eventService;
    private final @NotNull LoginService loginService;

    private @NotNull String pageTitle = "";

    public CommunityDetailView(final @NotNull ConfigurationService configurationService,
                               final @NotNull CommunityService communityService,
                               final @NotNull MemberService memberService,
                               final @NotNull EventService eventService,
                               final @NotNull LoginService loginService) {
        super(configurationService);
        this.communityService = communityService;
        this.memberService = memberService;
        this.eventService = eventService;
        this.loginService = loginService;
        addClassName("community-detail-view");
        add(pageContent);
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var profile = params.get("profile").orElse("");
        final var ui = beforeEnterEvent.getUI();
        final var locale = ui.getLocale();

        communityService.getCommunityWithImage(profile).ifPresentOrElse(communityWithImage -> {
            showDetails(communityWithImage, locale);
            pageTitle = communityWithImage.community().name();
        }, () -> {
            LOGGER.warn("Community not found with profile '{}'!", profile);
            beforeEnterEvent.rerouteToError(NotFoundException.class);
        });
    }

    private void showDetails(final @NotNull CommunityWithImageDto communityWithImage,
                             final @NotNull Locale locale) {
        pageContent.removeAll();

        final var community = communityWithImage.community();
        final var image = communityWithImage.image();

        if (image != null) {
            final var imageUrl = ImageUtil.resolveImageUrl(image);
            final var altText = getTranslation(locale, "community.boundary.CommunityDetailView.profileImage", community.name());
            final var htmlImage = new Image(imageUrl, altText);
            htmlImage.addClassName("community-image");
            pageContent.add(htmlImage);
        }

        final var name = new H2(community.name());
        name.addClassName("community-name");
        pageContent.add(name);

        final var profile = new Paragraph(community.profile());
        profile.addClassName("community-profile");
        pageContent.add(profile);

        final var prettyTime = new PrettyTime(locale);
        final var createdText = getTranslation(locale, "community.boundary.CommunityDetailView.created",
                prettyTime.format(community.created()));
        final var created = new Paragraph(createdText);
        created.addClassName("community-created");
        pageContent.add(created);

        final var description = new Markdown(community.description());
        description.addClassName("community-description");
        pageContent.add(description);

        final var memberCount = memberService.getMemberCount(community.id());
        final var memberCountText = getTranslation(locale, "community.boundary.CommunityDetailView.memberCount",
                Map.of("count", memberCount));
        final var memberCountParagraph = new Paragraph(memberCountText);
        memberCountParagraph.addClassName("community-memberCount");
        pageContent.add(memberCountParagraph);

        final var joinButton = new Button(getTranslation(locale, "community.boundary.CommunityDetailView.joinButton"));
        joinButton.addClickListener(_ -> {
            final var loggedInUser = loginService.getLoggedInUser();
            if (loggedInUser.isPresent()) {
                final var confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader(community.name());
                confirmDialog.setText("Do you want to join this community and become a member? The organizers will be "
                        + "able to see your profile and your name, but not your email address.");
                confirmDialog.setCancelable(true);
                confirmDialog.setCancelText("No");
                confirmDialog.setConfirmButton("Yes, join", _ -> {
                    memberService.joinCommunityWithUser(loggedInUser.orElseThrow(), community, locale);
                    showDetails(communityWithImage, locale); // update view
                });
                confirmDialog.open();
            } else {
                memberService.joinCommunityStartConfirmationProcess(community, locale);
            }
        });
        joinButton.addClassName("join-button");
        pageContent.add(joinButton);

        final var upcomingEventsPlaceholder = new Div();
        upcomingEventsPlaceholder.add(getUpcomingEventsComponent(community, locale));
        final var pastEventsPlaceholder = new Div();

        final var tabEvents = new TabSheet();
        tabEvents.add(
                getTranslation(locale, "community.boundary.CommunityDetailView.upcomingEvents"),
                upcomingEventsPlaceholder);
        final var pastEventsTab = tabEvents.add(
                getTranslation(locale, "community.boundary.CommunityDetailView.pastEvents"),
                pastEventsPlaceholder);

        tabEvents.addSelectedChangeListener(changeEvent -> {
            if (changeEvent.getSelectedTab() == pastEventsTab) {
                pastEventsPlaceholder.removeAll();
                pastEventsPlaceholder.add(getPastEventsComponent(community, locale));
            } else {
                upcomingEventsPlaceholder.removeAll();
                upcomingEventsPlaceholder.add(getUpcomingEventsComponent(community, locale));
            }
        });

        tabEvents.setWidthFull();
        pageContent.add(tabEvents);

        loginService.getLoggedInUser()
                .ifPresent(
                        user -> {
                            if (communityService.canCreateNewEvents(user)) {
                                pageContent.add(new Button(
                                        getTranslation("community.boundary.CommunityDetailView.createEventButton"),
                                        _ -> {
                                    final var communityId = Objects.requireNonNull(community.id()).toString();
                                    final var params = QueryParameters.of("communityId", communityId);
                                    UI.getCurrent().navigate(CreateEventView.class, params);
                                }));
                            }
                        }
                );
    }

    private Component getUpcomingEventsComponent(final @NotNull CommunityDto community,
                                                 final @NotNull Locale locale) {
        final var events = eventService.getUpcomingEventsWithImage(community);
        if (events.isEmpty()) {
            return new Paragraph(getTranslation(locale, "community.boundary.CommunityDetailView.noUpcomingEvents"));
        }
        return new EventGrid(events);
    }

    private Component getPastEventsComponent(final @NotNull CommunityDto community,
                                                 final @NotNull Locale locale) {
        final var events = eventService.getPastEventsWithImage(community);
        if (events.isEmpty()) {
            return new Paragraph(getTranslation(locale, "community.boundary.CommunityDetailView.noPastEvents"));
        }
        return new EventGrid(events);
    }

    @Override
    protected @NotNull String getViewTitle() {
        return pageTitle;
    }

}
