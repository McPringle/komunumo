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
package app.komunumo.ui.signals;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationSignalTest {

    @Test
    void uninitialized_isNotAuthenticated_andNotAdmin() {
        final var signal = new AuthenticationSignal();

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }


    @Test
    void setAuthenticated_false_resultsInNotAuthenticated_andNotAdmin() {
        final var signal = new AuthenticationSignal();

        signal.setAuthenticated(false);

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }


    @Test
    void setAuthenticated_true_resultsInAuthenticated_andNotAdmin() {
        final var signal = new AuthenticationSignal();

        signal.setAuthenticated(true);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void setAuthenticated_trueAndAdmin_resultsInAuthenticated_andAdmin() {
        final var signal = new AuthenticationSignal();

        signal.setAuthenticated(true, true);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isTrue();
    }

    @Test
    void setAuthenticated_trueAndNotAdmin_resultsInAuthenticated_andNotAdmin() {
        final var signal = new AuthenticationSignal();

        signal.setAuthenticated(true, false);

        assertThat(signal.isAuthenticated()).isTrue();
        assertThat(signal.isAdmin()).isFalse();
    }

    @Test
    void setAuthenticated_falseAndAdmin_resultsInNotAuthenticated_andNotAdmin() {
        final var signal = new AuthenticationSignal();

        signal.setAuthenticated(false, true);

        assertThat(signal.isAuthenticated()).isFalse();
        assertThat(signal.isAdmin()).isFalse();
    }

}
