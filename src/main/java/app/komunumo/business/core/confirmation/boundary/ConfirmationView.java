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
package app.komunumo.business.core.confirmation.boundary;

import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.core.confirmation.control.ConfirmationService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.components.PersistentNotification;
import app.komunumo.business.core.layout.boundary.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@Route(value = "confirm", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class ConfirmationView extends AbstractView implements AfterNavigationObserver {

    private static final int NOTIFICATION_DURATION = 10_000;

    private final transient @NotNull ConfirmationService confirmationService;

    public ConfirmationView(final @NotNull ConfigurationService configurationService,
                            final @NotNull ConfirmationService confirmationService) {
        super(configurationService);
        this.confirmationService = confirmationService;
        addClassName("confirmation-view");
        add(new H2(getTranslation("ui.views.confirmation.ConfirmationView.title")));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("ui.views.confirmation.ConfirmationView.title");
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
        final var message = confirmationResult.message();
        final var location = confirmationResult.location();

        // explicit switch expression to force compile error on status enum modification
        final Runnable checkStatus = switch (status) {
            case SUCCESS -> () -> {
                final var notification = new Notification(message);
                notification.addThemeVariants(status.getNotificationVariant());
                notification.setDuration(NOTIFICATION_DURATION);
                notification.open();
                if (!location.isBlank()) {
                    UI.getCurrent().navigate(location);
                }
            };
            case WARNING -> () -> {
                final var notification = new PersistentNotification(message);
                notification.addThemeVariants(status.getNotificationVariant());
                notification.open();
                if (!location.isBlank()) {
                    UI.getCurrent().navigate(location);
                }
            };
            case ERROR -> () -> {
                final var notification = new PersistentNotification(message);
                notification.addThemeVariants(status.getNotificationVariant());
                notification.open();
            };
        };
        checkStatus.run();
    }

}
