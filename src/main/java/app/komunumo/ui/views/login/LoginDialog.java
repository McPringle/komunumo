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

import app.komunumo.data.service.ConfirmationContext;
import app.komunumo.data.service.ConfirmationResult;
import app.komunumo.data.service.LoginService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.ui.components.ConfirmationDialog;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;

public final class LoginDialog extends ConfirmationDialog {

    private final @NotNull LoginService loginService;

    public LoginDialog(final @NotNull ServiceProvider serviceProvider)  {
        super(serviceProvider);
        this.loginService = serviceProvider.loginService();

        setHeaderTitle(getTranslation("ui.views.login.LoginDialog.title"));
        setCustomMessage(getTranslation("ui.views.login.LoginDialog.actionText"));
    }

    @Override
    protected @NotNull ConfirmationResult onConfirmationSuccess(final @NotNull String email,
                                                                final @NotNull ConfirmationContext confirmationContext) {
        if (loginService.login(email)) {
            UI.getCurrent().getPage()
                    .executeJs("setTimeout(function() { window.location.href = '/'; }, 5000);");
            return new ConfirmationResult(ConfirmationResult.Type.ERROR,
                    getTranslation("ui.views.login.LoginDialog.successMessage"));
        }
        return new ConfirmationResult(ConfirmationResult.Type.ERROR,
                getTranslation("ui.views.login.LoginDialog.failedMessage"));
    }

}
