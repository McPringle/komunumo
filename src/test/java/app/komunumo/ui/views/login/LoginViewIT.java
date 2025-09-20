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

import app.komunumo.ui.IntegrationTest;
import app.komunumo.ui.components.ConfirmationDialog;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class LoginViewIT extends IntegrationTest {

    @Test
    void loginDialogShouldOpenAutomatically() {
        UI.getCurrent().navigate(LoginView.class);

        MockVaadin.clientRoundtrip();

        final var loginDialog = _get(ConfirmationDialog.class);
        assertThat(loginDialog).isNotNull();
        assertThat(loginDialog.isOpened()).isTrue();
    }

    @Test
    void loginDialogIsClosable() {
        UI.getCurrent().navigate(LoginView.class);

        MockVaadin.clientRoundtrip();

        final var loginDialog = _get(ConfirmationDialog.class);
        assertThat(loginDialog).isNotNull();
        assertThat(loginDialog.isOpened()).isTrue();

        final var closeButton = _get(loginDialog, Button.class, spec -> spec.withClasses("close-dialog-button"));
        assertThat(closeButton).isNotNull();
        _click(closeButton);

        MockVaadin.clientRoundtrip();

        assertThat(loginDialog.isOpened()).isFalse();
    }

    @Test
    void loginDialogCanBeCanceled() {
        UI.getCurrent().navigate(LoginView.class);

        MockVaadin.clientRoundtrip();

        final var loginDialog = _get(ConfirmationDialog.class);
        assertThat(loginDialog).isNotNull();
        assertThat(loginDialog.isOpened()).isTrue();

        final var cancelButton = _get(loginDialog, Button.class, spec -> spec.withText("Cancel"));
        assertThat(cancelButton).isNotNull();
        _click(cancelButton);

        MockVaadin.clientRoundtrip();

        assertThat(loginDialog.isOpened()).isFalse();
    }

}
