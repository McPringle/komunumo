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
package app.komunumo.ui.views.error;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;

@Route(value = "error/404", layout = WebsiteLayout.class)
public final class NotFoundView extends ErrorView implements HasErrorParameter<NotFoundException> {

    public NotFoundView(final @NotNull ConfigurationService configurationService) {
        super(ErrorType.NOT_FOUND, configurationService);
    }

    @Override
    public int setErrorParameter(final @NotNull BeforeEnterEvent beforeEnterEvent,
                                 final @NotNull ErrorParameter<NotFoundException> errorParameter) {
        return HttpServletResponse.SC_NOT_FOUND;
    }

}
