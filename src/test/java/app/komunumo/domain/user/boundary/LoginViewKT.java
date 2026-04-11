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

import app.komunumo.SecurityConfig;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.infra.ui.vaadin.components.KomunumoMessageBox;
import app.komunumo.test.KaribuTest;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import org.junit.jupiter.api.Test;

import static app.komunumo.domain.user.entity.UserRole.USER;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class LoginViewKT extends KaribuTest {

    @Test
    void redirectWhenAlreadyLoggedIn() {
        login(getTestUser(USER));
        UI.getCurrent().navigate(LoginView.class);
        MockVaadin.clientRoundtrip(false);
        assertThat(KaribuTest.currentViewClass())
                .isNotEqualTo(LoginView.class);
    }

    @Test
    void confirmWithInvalidConfirmationId_shouldShowError() {
        UI.getCurrent().navigate(SecurityConfig.LOGIN_URL,
                QueryParameters.of(LoginService.CONFIRMATION_PARAMETER, "invalid"));
        final var messageBox = _get(KomunumoMessageBox.class);
        assertThat(messageBox.getText())
                .isEqualTo("The confirmation link is invalid or has expired.  \nPlease request a new one on this page.");
    }
}
