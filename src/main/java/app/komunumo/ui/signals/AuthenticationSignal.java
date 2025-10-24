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

import app.komunumo.util.SecurityUtil;
import com.vaadin.signals.ValueSignal;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * <p>Publishes authentication and authorization state for reactive UI updates.</p>
 *
 * <p>This simplified signal tracks whether a user is authenticated and whether the user
 * has the ADMIN role. Components can observe these signals to adjust visibility and behavior.</p>
 *
 * <p>This bean is session scoped so each user session has an independent state that mirrors the
 * Spring Security context.</p>
 */
@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationSignal {

    private final @NotNull ValueSignal<Boolean> authenticated = new ValueSignal<>(false);
    private final @NotNull ValueSignal<Boolean> admin = new ValueSignal<>(false);
    private final @NotNull ValueSignal<Boolean> localUser = new ValueSignal<>(false);

    /**
     * <p>Refreshes the signal from the current Spring Security context.</p>
     *
     * <p>This method reads the active authentication and updates the internal flags accordingly.
     * Call this at the start of a request or before rendering auth-dependent UI.</p>
     */
    public void refreshFromSecurityContext() {
        final var isAuthenticated = SecurityUtil.isLoggedIn();
        final var isAdmin = SecurityUtil.isAdmin();
        final var isLocalUser = SecurityUtil.isLocalUser();

        setAuthenticated(isAuthenticated, isAdmin, isLocalUser);
    }

    /**
     * <p>Sets authentication based on the given state.</p>
     *
     * @param isAuthenticated true if authenticated, false otherwise
     */
    public void setAuthenticated(final boolean isAuthenticated) {
        setAuthenticated(isAuthenticated, false, false);
    }

    /**
     * <p>Sets authentication and admin flags based on the given state.</p>
     *
     * @param isAuthenticated true if authenticated, false otherwise
     * @param isAdmin true if the authenticated user has ADMIN privileges
     * @param isLocalUser true if the authenticated user has USER privileges and is of type LOCAL
     */
    public void setAuthenticated(final boolean isAuthenticated, final boolean isAdmin, final boolean isLocalUser) {
        authenticated.value(isAuthenticated);
        admin.value(isAuthenticated && isAdmin);
        localUser.value(isAuthenticated && isLocalUser);
    }

    /**
     * <p>Returns whether the current user is authenticated.</p>
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return Boolean.TRUE.equals(authenticated.value());
    }


    /**
     * <p>Returns whether the current user has ADMIN privileges.</p>
     *
     * @return true if authenticated and ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return isAuthenticated() && Boolean.TRUE.equals(admin.value());
    }

    /**
     * <p>Returns whether the current user has USER privileges and is of type LOCAL.</p>
     *
     * @return true if authenticated and USER and of type LOCAL, false otherwise
     */
    public boolean isLocalUser() {
        return isAuthenticated() && Boolean.TRUE.equals(localUser.value());
    }

}
