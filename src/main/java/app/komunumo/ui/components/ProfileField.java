package app.komunumo.ui.components;

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.service.ConfigurationService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProfileField extends Div {

    private static final String USERNAME_PATTERN = "[a-zA-Z0-9_]";
    private static final String DOMAIN_PATTERN = "[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
    private static final String PROFILE_PATTERN = "^@" + USERNAME_PATTERN + "+@" + DOMAIN_PATTERN + "$";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileField.class);

    private static final int MIN_LENGTH = 1;
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
            if (value.isEmpty() || value.length() > MAX_LENGTH) {
                showErrorMessage("The profile name must be between %d and %d characters long".formatted(MIN_LENGTH, MAX_LENGTH));
            } else if (profileNameAvailabilityChecker.isProfileNameAvailable(getValue())){
                showSuccessMessage("This profile name is available");
            } else {
                showErrorMessage("This profile name is not available");
            }
        });

        add(textField, message);
    }

    private void showSuccessMessage(final @NotNull String successMessage) {
        message.setText("✅ %s".formatted(successMessage));
        message.removeClassName("error");
    }

    private void showErrorMessage(final @NotNull String errorMessage) {
        message.setText("\uD83D\uDD34 %s".formatted(errorMessage));
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
            LOGGER.error("The format of the profile name is invalid: '{}'", value);
            showErrorMessage("The format of the profile name is invalid: '%s'".formatted(value));
            textField.setReadOnly(true);
        }

        final var parts = value.split("@", 3);
        final var localPart = parts[1];
        final var domainPart = parts[2];
        if (!domainPart.equals(domainName)) {
            LOGGER.error("The domain name '{}' is not the domain name of this instance ('{}')!", domainPart, domainName);
            showErrorMessage("The domain name '%s' is not the domain name of this instance ('%s')!".formatted(domainPart, domainName));
            textField.setReadOnly(true);
        }

        if (!textField.isReadOnly()) {
            textField.setValue(localPart);
        }
    }

    public void setLabel(final @NotNull String label) {
        textField.setLabel(label);
    }

    public void setRequired(final boolean required) {
        textField.setRequired(required);
    }

    @FunctionalInterface
    public interface ProfileNameAvailabilityChecker {
        boolean isProfileNameAvailable(String profileName);
    }

}
