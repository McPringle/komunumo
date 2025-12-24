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
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Route(value = "logout", layout = WebsiteLayout.class)
@PermitAll
public final class LogoutView extends AbstractView implements BeforeEnterObserver {

    private final @NotNull LoginService loginService;

    public LogoutView(final @NotNull ConfigurationService configurationService,
                      final @NotNull LoginService loginService) {
        super(configurationService);
        this.loginService = loginService;
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("user.boundary.LogoutView.title");
    }

    @Override
    public void beforeEnter(final @Nullable BeforeEnterEvent event) {
        loginService.logout();
    }
}
