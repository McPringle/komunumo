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
package app.komunumo.vaadin.components;

import app.komunumo.domain.core.config.entity.ConfigurationSetting;
import app.komunumo.domain.core.config.control.ConfigurationService;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import org.jetbrains.annotations.NotNull;

public final class ProfileField extends Div implements HasValue<HasValue.ValueChangeEvent<String>, String> {

    private static final String USERNAME_PATTERN = "[a-zA-Z0-9_]";
    private static final String DOMAIN_PATTERN = "[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
    private static final String PROFILE_PATTERN = "^@" + USERNAME_PATTERN + "+@" + DOMAIN_PATTERN + "$";

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;

    private final TextField textField = new TextField();
    private final Paragraph message = new Paragraph();
    private final String domainName;

    private String originalValue = "";

    public ProfileField(final @NotNull ConfigurationService configurationService,
                        final @NotNull ProfileNameAvailabilityChecker profileNameAvailabilityChecker) {
        super();
        addClassName("profile-field");
        setWidthFull();

        message.setClassName("profile-message");
        domainName = configurationService.getConfiguration(ConfigurationSetting.INSTANCE_DOMAIN);

        textField.setMinLength(MIN_LENGTH);
        textField.setMaxLength(MAX_LENGTH);
        textField.setAllowedCharPattern(USERNAME_PATTERN);

        textField.setPrefixComponent(new Span("@"));
        textField.setSuffixComponent(new Span("@" + domainName));

        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.addValueChangeListener(_ -> {
            final var value = textField.getValue();
            if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
                showErrorMessage(getTranslation("ui.components.ProfileField.errorLength", MIN_LENGTH, MAX_LENGTH));
            } else if (profileNameAvailabilityChecker.isProfileNameAvailable(getValue())) {
                showSuccessMessage(getTranslation("ui.components.ProfileField.usernameAvailable"));
            } else {
                showErrorMessage(getTranslation("ui.components.ProfileField.usernameNotAvailable"));
            }
        });

        add(textField, message);
    }

    @SuppressWarnings("SameParameterValue")
    private void showSuccessMessage(final @NotNull String successMessage) {
        message.setText(successMessage);
        message.removeClassName("error");
        message.addClassName("success");
    }

    private void showErrorMessage(final @NotNull String errorMessage) {
        message.setText(errorMessage);
        message.removeClassName("success");
        message.addClassName("error");
    }

    public String getValue() {
        if (textField.isReadOnly()) {
            return originalValue;
        }
        return "@%s@%s".formatted(textField.getValue(), domainName);
    }

    public void setValue(final @NotNull String value) {
        originalValue = value;

        if (!value.matches(PROFILE_PATTERN)) {
            showErrorMessage(getTranslation("ui.components.ProfileField.syntaxError", value));
            setReadOnly(true);
            return;
        }

        final var parts = value.split("@", 3);
        final var localPart = parts[1];
        final var domainPart = parts[2];
        if (!domainPart.equals(domainName)) {
            showErrorMessage(getTranslation("ui.components.ProfileField.domainError", domainPart, domainName));
            textField.setReadOnly(true);
            return;
        }

        textField.setValue(localPart);
    }

    public void setLabel(final @NotNull String label) {
        textField.setLabel(label);
    }


    @Override
    public Registration addValueChangeListener(final @NotNull ValueChangeListener<? super ValueChangeEvent<String>> valueChangeListener) {
        return textField.addValueChangeListener(valueChangeListener);
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        textField.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return textField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
        textField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return textField.isRequiredIndicatorVisible();
    }

    public void setRequired(final boolean required) {
        textField.setRequired(required);
    }

    public boolean isRequired() {
        return textField.isRequired();
    }

    @FunctionalInterface
    public interface ProfileNameAvailabilityChecker {
        boolean isProfileNameAvailable(String profileName);
    }

}
