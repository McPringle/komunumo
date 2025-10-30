package app.komunumo.ui.components;

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileField extends Div {

    private final TextField textField = new TextField();
    private final String domainName;

    public ProfileField(@NotNull ConfigurationService configurationService, @NotNull CommunityService communityService) {
        super();
        addClassName("profile-field");
        setWidthFull();
        textField.setPrefixComponent(new Span("@"));
        var instanceUrl = configurationService.getConfiguration(ConfigurationSetting.INSTANCE_URL);
        domainName = extractDomainName(instanceUrl);
        textField.setSuffixComponent(new Span("@" + domainName));
        textField.setAllowedCharPattern("[a-zA-Z0-9_]");
        textField.setMinLength(1);
        textField.setMaxLength(30);
        textField.setWidthFull();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        var profileValue = new Paragraph();
        var isProfileNameAvailibaleParagraph = new Paragraph();


        add(textField, profileValue, isProfileNameAvailibaleParagraph);
        textField.addValueChangeListener(e -> {
            profileValue.setText(getValue());
            if (communityService.isProfileNameAvailable(getValue())){
                isProfileNameAvailibaleParagraph.setText("profile name is available");
            } else {
                isProfileNameAvailibaleParagraph.setText("profile name is not available");
            }
        });

    }

    private String extractDomainName(String url) {
        Pattern p = Pattern.compile("^(?i)https?://(?:[^@/]+@)?([^:/?#]+)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    public String getValue() {
        return "@" + textField.getValue() + "@" + domainName;
    }

    public void setValue(String value) {
        String[] parts = value.split("@");
        textField.setValue(parts[1]);
    }
}
