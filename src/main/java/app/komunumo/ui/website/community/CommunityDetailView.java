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
package app.komunumo.ui.website.community;

import app.komunumo.data.dto.CommunityWithImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.component.AbstractView;
import app.komunumo.ui.website.WebsiteLayout;
import app.komunumo.util.ImageUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Route(value = "communities/:profile", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class CommunityDetailView extends AbstractView implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityDetailView.class);

    private final transient @NotNull CommunityService communityService;

    private final @NotNull HtmlContainer pageContent = new Div();

    private @NotNull String pageTitle = "";

    public CommunityDetailView(final @NotNull ServiceProvider serviceProvider) {
        super(serviceProvider.configurationService());
        this.communityService = serviceProvider.communityService();
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
            final var altText = getTranslation(locale, "communities.profileImage.altText", community.name());
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

        final var description = new Markdown(community.description());
        description.addClassName("community-description");
        pageContent.add(description);
    }

    @Override
    protected @NotNull String getViewTitle() {
        return pageTitle;
    }

}
