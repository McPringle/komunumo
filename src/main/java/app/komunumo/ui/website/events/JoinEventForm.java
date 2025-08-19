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
package app.komunumo.ui.website.events;

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.service.ParticipationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;

import static com.vaadin.flow.component.details.DetailsVariant.FILLED;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public final class JoinEventForm extends Details {

    private final transient @NotNull ParticipationService participationService;
    private final transient @NotNull EventDto event;

    public JoinEventForm(final @NotNull ParticipationService participationService,
                         final @NotNull EventDto event) {
        super();
        this.participationService = participationService;
        this.event = event;
        addClassName("join-event-form");
        addThemeVariants(FILLED);
        setSummaryText(getTranslation("event.join.title"));
        showEnterEmailForm();
    }

    private void showEnterEmailForm() {
        removeAll();

        final var joinInfo = new Paragraph(getTranslation("event.join.email.info"));
        add(joinInfo);

        final var emailField = new EmailField();
        emailField.setPlaceholder(getTranslation("event.join.email.field"));
        emailField.setValueChangeMode(EAGER);
        emailField.setWidthFull();
        add(emailField);

        final var emailButton = new Button(getTranslation("event.join.email.button"));
        add(emailButton);

        final var binder = new Binder<>(EmailBean.class);
        binder.forField(emailField)
                .withValidator(new EmailValidator(getTranslation("validation.email.invalid"), true))
                .bind(EmailBean::getEmail, EmailBean::setEmail);
        binder.addStatusChangeListener(evt ->
                emailButton.setEnabled(!emailField.getValue().isBlank() && binder.isValid()));
        binder.setBean(new EmailBean());
        binder.validate();

        emailButton.addClickListener(evt -> {
            final var email = binder.getBean().getEmail();
            final var locale = getLocale();
            if (participationService.requestVerificationCode(event, email, locale)) {
                showEnterCodeForm(email);
            } else {
                emailField.setErrorMessage(getTranslation("event.join.email.error"));
                emailField.setInvalid(true);
                emailField.focus();
            }
        });
    }

    private void showEnterCodeForm(final @NotNull String email) {
        removeAll();
        add(new Paragraph(getTranslation("event.join.email.send", email)));
    }

    public static final class EmailBean {
        private @NotNull String email = "";

        public @NotNull String getEmail() {
            return email;
        }

        public void setEmail(final @NotNull String email) {
            this.email = email;
        }
    }

}
