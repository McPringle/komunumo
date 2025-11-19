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
package app.komunumo.business.page.boundary;

import app.komunumo.business.page.entity.GlobalPageDto;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.page.control.GlobalPageService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.business.core.layout.boundary.WebsiteLayout;
import app.komunumo.util.SecurityUtil;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "page/:slot", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class GlobalPageView extends AbstractView implements BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPageView.class);

    private final transient @NotNull GlobalPageService globalPageService;

    private final @NotNull HtmlContainer pageContent = new Div();

    private @NotNull String pageTitle = "";
    private @Nullable ContextMenu contextMenu;

    public GlobalPageView(final @NotNull ConfigurationService configurationService,
                          final @NotNull GlobalPageService globalPageService) {
        super(configurationService);
        this.globalPageService = globalPageService;
        addClassName("global-page-view");
        pageContent.setClassName("global-page-content");
        add(pageContent);
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var params = beforeEnterEvent.getRouteParameters();
        final var slot = params.get("slot").orElse("");
        final var ui = beforeEnterEvent.getUI();
        final var locale = ui.getLocale();

        globalPageService.getGlobalPage(slot, locale).ifPresentOrElse(this::renderPage, () -> {
            LOGGER.warn("No global page found with slot '{}' and locale '{}'!", slot, locale);
            beforeEnterEvent.rerouteToError(NotFoundException.class);
        });
    }

    private void renderPage(final @NotNull GlobalPageDto globalPage) {
        pageContent.removeAll();
        final var markdownContent = new Markdown(globalPage.markdown());
        pageContent.add(markdownContent);
        pageContent.setWidthFull();
        pageTitle = globalPage.title();
        updatePageTitle();

        if (SecurityUtil.isAdmin()) {
            contextMenu = new ContextMenu(markdownContent);
            contextMenu.addItem(getTranslation("ui.views.page.GlobalPageView.edit"), _ ->
                    new GlobalPageEditorDialog(globalPageService, globalPage, this::renderPage)
                            .open());
        }
    }

    @Override
    protected @NotNull String getViewTitle() {
        return pageTitle;
    }

    @VisibleForTesting
    @Nullable ContextMenu getContextMenu() {
        return contextMenu;
    }
}
