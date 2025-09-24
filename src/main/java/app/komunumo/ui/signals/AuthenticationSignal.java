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

import com.vaadin.signals.ValueSignal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public final class AuthenticationSignal {

  private final @NotNull ValueSignal<Boolean> authenticated = new ValueSignal<>(Boolean.class);

  public void setAuthenticated(final boolean value) {
      authenticated.value(value);
  }

  public boolean isAuthenticated() {
      return Boolean.TRUE.equals(authenticated.value());
  }

}
