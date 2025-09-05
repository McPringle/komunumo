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

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.service.ConfirmationContext;
import app.komunumo.data.service.ConfirmationResult;
import app.komunumo.data.service.ParticipationService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.TranslationProvider;
import app.komunumo.ui.components.ConfirmationDialog;
import app.komunumo.util.LinkUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public final class RegisterDialog extends ConfirmationDialog {

    private static @NotNull final String CONTEXT_KEY_EVENT = "event";

    private final @NotNull ParticipationService participationService;
    private final @NotNull Locale locale;

    public RegisterDialog(final @NotNull ServiceProvider serviceProvider,
                          final @NotNull TranslationProvider translationProvider,
                          final @NotNull Locale locale,
                          final @NotNull EventDto event)  {
        super(serviceProvider);
        this.participationService = serviceProvider.participationService();
        this.locale = getLocale();

        setHeaderTitle(getTranslation("ui.views.events.RegisterDialog.title"));
        setCustomMessage(getTranslation("ui.views.events.RegisterDialog.infoText"));
        setContext(ConfirmationContext.of(Map.entry(CONTEXT_KEY_EVENT, event)));
    }

    @Override
    protected @NotNull ConfirmationResult onConfirmationSuccess(final @NotNull String email,
                                                                final @NotNull ConfirmationContext context) {
        final var event = (EventDto) context.get(CONTEXT_KEY_EVENT);
        participationService.joinEvent(event, email, locale);
        return new ConfirmationResult(ConfirmationResult.Type.SUCCESS,
                getTranslation("ui.views.events.RegisterDialog.successMessage",
                        event.title(), LinkUtil.getLink(event)));
    }

}
