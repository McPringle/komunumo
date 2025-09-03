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
import app.komunumo.data.service.ConfirmationService;
import app.komunumo.data.service.ParticipationService;
import app.komunumo.data.service.interfaces.ConfirmationHandler;
import app.komunumo.util.LinkUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;

import static com.vaadin.flow.component.details.DetailsVariant.FILLED;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class RegisterForm extends Details {

    private final transient @NotNull ConfirmationService confirmationService;
    private final transient @NotNull ParticipationService participationService;
    private final transient @NotNull EventDto event;

    public RegisterForm(final @NotNull ConfirmationService confirmationService,
                        final @NotNull ParticipationService participationService,
                        final @NotNull EventDto event) {
        super();
        this.confirmationService = confirmationService;
        this.participationService = participationService;
        this.event = event;
        addClassName("register-form");
        addThemeVariants(FILLED);
        setSummaryText(getTranslation("event.register.title"));
        showEnterEmailForm();
    }

    private void showEnterEmailForm() {
        removeAll();

        final var confirmationTimeout = confirmationService.getConfirmationTimeoutText(getLocale());
        final var infoText = new Paragraph(getTranslation("event.register.email.info", confirmationTimeout));
        add(infoText);

        final var emailField = new EmailField();
        emailField.setPlaceholder(getTranslation("event.register.email.field"));
        emailField.setValueChangeMode(EAGER);
        emailField.setWidthFull();
        add(emailField);

        final var emailButton = new Button(getTranslation("event.register.email.button"));
        add(emailButton);

        final var binder = new Binder<DummyBean>();
        binder.forField(emailField)
                .withValidator(new EmailValidator(getTranslation("ui.validation.email.invalid"), true))
                .bind(dummy -> null, (dummy, value) -> { });
        binder.setBean(new DummyBean());
        binder.addStatusChangeListener(evt ->
                emailButton.setEnabled(!emailField.getValue().isBlank() && binder.isValid())
        );
        binder.validate();

        emailButton.addClickListener(evt -> {
            final var locale = getLocale();
            final var eventTitle = event.title();
            final var eventLink = "[%s](%s)".formatted(eventTitle, LinkUtil.getLink(event));
            final var emailAddress = emailField.getValue().trim().toLowerCase(locale);
            final var confirmationReason = getTranslation("confirmation.reason.event.register", eventTitle);
            final var onFailMessage = getTranslation("event.register.failed", eventLink);
            final var onSuccessMessage = getTranslation("event.register.success", eventLink);
            final ConfirmationHandler confirmationHandler = confirmationContext ->
                    participationService.joinEvent(event, emailAddress, locale);

            confirmationService.startConfirmationProcess(
                    emailAddress,
                    confirmationReason,
                    onSuccessMessage,
                    onFailMessage,
                    confirmationHandler,
                    locale);

            removeAll();
            add(new Paragraph(getTranslation("event.register.email.send", emailAddress, confirmationTimeout)));
        });

        addOpenedChangeListener(evt -> {
            if (evt.isOpened()) {
                emailField.focus();
            }
        });
    }

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean { }

}
