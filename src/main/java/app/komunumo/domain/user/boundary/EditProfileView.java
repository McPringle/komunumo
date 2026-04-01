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
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

@Route(value = "settings/profile", layout = WebsiteLayout.class)
@RolesAllowed("USER_LOCAL")
public class EditProfileView extends AbstractView {

    /**
     * <p>Creates a new view instance with access to the configuration service for
     * retrieving localized configuration values such as the instance name.</p>
     *
     * @param configurationService the configuration service used to resolve the instance name;
     *                             must not be {@code null}
     */
    public EditProfileView(final @NotNull ConfigurationService configurationService) {
        super(configurationService);
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("user.boundary.EditProfileView.title");
    }

}
