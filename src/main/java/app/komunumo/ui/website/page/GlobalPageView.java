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
package app.komunumo.ui.website.page;

import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.component.AbstractView;
import app.komunumo.ui.website.WebsiteLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static app.komunumo.util.MarkdownUtil.convertMarkdownToHtml;

@Route(value = "page/:slot", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class GlobalPageView extends AbstractView implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPageView.class);

    private final transient @NotNull GlobalPageService globalPageService;

    private @NotNull String pageTitle = "";

    public GlobalPageView(final @NotNull ServiceProvider serviceProvider) {
        super(serviceProvider.configurationService());
        this.globalPageService = serviceProvider.globalPageService();
        addClassName("global-page-view");
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var slot = params.get("slot").orElse("");
        final var ui = beforeEnterEvent.getUI();
        final var locale = ui.getLocale();

        globalPageService.getGlobalPage(slot, locale).ifPresentOrElse(globalPage -> {
            final var html = convertMarkdownToHtml(globalPage.markdown());
            add(new Html("<div>%s</div>".formatted(html)));
            pageTitle = globalPage.title();
        }, () -> {
            LOGGER.warn("No global page found with slot '{}' and locale '{}'!", slot, locale);
            beforeEnterEvent.rerouteToError(NotFoundException.class);
        });
    }

    @Override
    protected @NotNull String getViewTitle() {
        return pageTitle;
    }

}
