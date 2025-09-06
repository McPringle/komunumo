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
package app.komunumo.ui.views.login;

import app.komunumo.data.service.ServiceProvider;
import app.komunumo.security.SecurityConfig;
import app.komunumo.ui.TranslationProvider;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AnonymousAllowed
@Route(value = SecurityConfig.LOGIN_URL, layout = WebsiteLayout.class)
public final class LoginView extends AbstractView implements BeforeEnterObserver {

    private final @NotNull ServiceProvider serviceProvider;
    private final @NotNull TranslationProvider translationProvider;

    public LoginView(final @NotNull ServiceProvider serviceProvider,
                     final @NotNull TranslationProvider translationProvider) {
        super(serviceProvider.configurationService());
        this.serviceProvider = serviceProvider;
        this.translationProvider = translationProvider;
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "login.title");
    }

    @Override
    public void beforeEnter(final @Nullable BeforeEnterEvent beforeEnterEvent) {
        new LoginDialog(serviceProvider).open();
    }
}
