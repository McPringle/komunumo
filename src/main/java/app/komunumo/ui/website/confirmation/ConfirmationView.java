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
package app.komunumo.ui.website.confirmation;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.ConfirmationService;
import app.komunumo.ui.component.AbstractView;
import app.komunumo.ui.website.WebsiteLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "confirm", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class ConfirmationView extends AbstractView implements BeforeEnterObserver {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ConfirmationView.class);

    private final transient @NotNull ConfirmationService confirmationService;

    public ConfirmationView(final @NotNull ConfigurationService configurationService,
                            final @NotNull ConfirmationService confirmationService) {
        super(configurationService);
        this.confirmationService = confirmationService;
        addClassName("confirmation-view");
        add(new H2(getTranslation("confirmation.view.title")));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("confirmation.view.title");
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var confirmationId = beforeEnterEvent
                .getLocation()
                .getQueryParameters()
                .getSingleParameter("id")
                .orElseThrow(() -> new NotFoundException("Missing confirmation ID!"))
                .trim();

        final var success = confirmationService.confirm(confirmationId);
        if (success.isEmpty()) {
            LOGGER.warn("Confirmation with ID '{}' not found!", confirmationId);
            final var errorMessage = new Markdown(getTranslation("confirmation.view.error"));
            errorMessage.addClassName("error");
            add(errorMessage);
        } else {
            final var successMessage = new Markdown(success.orElseThrow());
            successMessage.addClassName("success-message");
            add(successMessage);
        }
    }

}
