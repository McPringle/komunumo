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

package app.komunumo.business.user.boundary;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.business.user.control.LoginService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jetbrains.annotations.NotNull;

@Route(value = "logout", layout = WebsiteLayout.class)
@PermitAll
public final class LogoutView extends AbstractView {

    public LogoutView(final @NotNull ConfigurationService configurationService,
                      final @NotNull LoginService loginService) {
        super(configurationService);
        loginService.logout();
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "ui.views.logout.title");
    }

}
