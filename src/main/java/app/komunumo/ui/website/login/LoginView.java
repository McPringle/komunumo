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
package app.komunumo.ui.website.login;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.security.SecurityConfig;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.website.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@Route(value = SecurityConfig.LOGIN_URL, layout = WebsiteLayout.class)
@AnonymousAllowed
public final class LoginView extends AbstractView {

    public LoginView(final @NotNull ConfigurationService configurationService) {
        super(configurationService);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        final var locale = UI.getCurrent().getLocale();
        final var i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle(getTranslation(locale, "login.title"));
        i18n.getForm().setUsername(getTranslation(locale, "login.form.email"));
        i18n.getForm().setPassword(getTranslation(locale, "login.form.password"));
        i18n.getForm().setSubmit(getTranslation(locale, "login.form.submit"));
        i18n.getForm().setForgotPassword(getTranslation(locale, "login.form.forgot-password"));

        final var login = new LoginForm();
        login.setI18n(i18n);
        login.setAction(SecurityConfig.LOGIN_URL);
        add(login);
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "login.title");
    }

}
