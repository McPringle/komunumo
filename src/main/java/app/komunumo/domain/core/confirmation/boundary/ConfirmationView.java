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
package app.komunumo.domain.core.confirmation.boundary;

import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.confirmation.control.ConfirmationService;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

import static app.komunumo.util.NotificationUtil.showNotification;

@Route(value = "confirm", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class ConfirmationView extends AbstractView implements AfterNavigationObserver {

    private final transient @NotNull ConfirmationService confirmationService;

    public ConfirmationView(final @NotNull ConfigurationService configurationService,
                            final @NotNull ConfirmationService confirmationService) {
        super(configurationService);
        this.confirmationService = confirmationService;
        addClassName("confirmation-view");
        add(new H2(getTranslation("core.confirmation.boundary.ConfirmationView.title")));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("core.confirmation.boundary.ConfirmationView.title");
    }

    @Override
    public void afterNavigation(final @NotNull AfterNavigationEvent afterNavigationEvent) {
        final var confirmationId = afterNavigationEvent
                .getLocation()
                .getQueryParameters()
                .getSingleParameter("id")
                .orElseThrow(() -> new NotFoundException("Missing confirmation ID!"))
                .trim();

        final var confirmationResult = confirmationService.confirm(confirmationId, getLocale());
        final var status = confirmationResult.confirmationStatus();
        //noinspection ExtractMethodRecommender // readability improvementk
        final var message = confirmationResult.message();
        final var location = confirmationResult.location();

        // explicit switch expression to force compile error on status enum modification
        final Runnable checkStatus = switch (status) {
            case SUCCESS, WARNING -> () -> {
                showNotification(message, status.getNotificationVariant());
                if (!location.isBlank()) {
                    UI.getCurrent().navigate(location);
                }
            };
            case ERROR -> () -> showNotification(message, status.getNotificationVariant());
        };
        checkStatus.run();
    }

}
