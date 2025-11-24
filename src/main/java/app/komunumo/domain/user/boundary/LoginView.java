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
package app.komunumo.domain.user.boundary;

import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.SecurityConfig;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.util.LocationUtil;
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

    private final @NotNull LoginService loginService;

    public LoginView(final @NotNull ConfigurationService configurationService,
                     final @NotNull LoginService loginService) {
        super(configurationService);
        this.loginService = loginService;
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "ui.views.login.title");
    }

    @Override
    public void beforeEnter(final @Nullable BeforeEnterEvent beforeEnterEvent) {
        final var ui = UI.getCurrent();
        loginService.startLoginProcess(ui.getLocale(), LocationUtil.getCurrentLocation(ui));
    }
}
