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
package app.komunumo.ui.views.events;

import app.komunumo.data.dto.ConfirmationContext;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.service.ParticipationService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.components.ConfirmationDialog;
import app.komunumo.util.LinkUtil;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RegisterDialog extends ConfirmationDialog {


    private final @NotNull ParticipationService participationService;
    private final @NotNull EventDto event;
    private final @NotNull Locale locale;

    public RegisterDialog(final @NotNull ServiceProvider serviceProvider,
                          final @NotNull EventDto event)  {
        super(
                serviceProvider,
                "ui.views.events.RegisterDialog.infoText",
                "ui.views.events.RegisterDialog.successMessage",
                "ui.views.events.RegisterDialog.failedMessage"
        );
        this.participationService = serviceProvider.participationService();
        this.event = event;
        this.locale = getLocale();

        setHeaderTitle(getTranslation("ui.views.events.RegisterDialog.title"));
        setCustomMessage(getTranslation("ui.views.events.RegisterDialog.infoText"));
    }

    @Override
    protected boolean onConfirmationSuccess(@NotNull ConfirmationContext confirmationContext) {
        final var email = confirmationContext.email();
        if (participationService.joinEvent(event, email, locale)) {
            final var redirectUrl = LinkUtil.getLink(event);
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(function() { window.location.href = '%s'; }, 5000);".formatted(redirectUrl));
            return true;
        }
        return false;
    }

}
