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
package app.komunumo.util;

import app.komunumo.vaadin.components.PersistentNotification;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.jetbrains.annotations.NotNull;

public final class NotificationUtil {

    /**
     * <p>Utility method to show a notification using the provided message and notification variant.</p>
     *
     * <p>Error and warning notifications are persistent and require explicit user confirmation. All other
     * notifications are automatically dismissed after ten seconds.</p>
     *
     * @param message          the message that will be shown in the notification
     * @param notificationVariant the notification variant that will define the theme of the notification
     */
    public static void showNotification(final @NotNull String message,
                                    final @NotNull NotificationVariant notificationVariant) {
        final var persistentNotification = notificationVariant.equals(NotificationVariant.LUMO_ERROR)
                || notificationVariant.equals(NotificationVariant.LUMO_WARNING);

        final var notification = persistentNotification
                ? new PersistentNotification(message)
                : new Notification(message);
        notification.addThemeVariants(notificationVariant);
        notification.setDuration(10_000);
        notification.open();
    }

    /**
     * <p>Private constructor to prevent instantiation of this utility class.</p>
     */
    private NotificationUtil() {
        throw new IllegalStateException("Utility class");
    }

}
