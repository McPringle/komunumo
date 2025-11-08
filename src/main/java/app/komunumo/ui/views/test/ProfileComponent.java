package app.komunumo.ui.views.test;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.UserService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.components.ProfileField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@AnonymousAllowed
@Route("test")
public class ProfileComponent extends AbstractView {

    /**
     * <p>Creates a new view instance with access to the configuration service for
     * retrieving localized configuration values such as the instance name.</p>
     *
     * @param configurationService the configuration service used to resolve the instance name;
     *                             must not be {@code null}
     */
    protected ProfileComponent(final @NotNull ConfigurationService configurationService,
                               final @NotNull UserService userService) {
        super(configurationService);
        final var profileField = new ProfileField(configurationService, userService::isProfileNameAvailable);
        add(profileField);
        profileField.setValue("@random@example.com");
    }

    @Override
    protected @NotNull String getViewTitle() {
        return "Test";
    }
}
