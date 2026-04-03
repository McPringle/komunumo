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
package app.komunumo.domain.user.entity;

import app.komunumo.util.SecurityUtil;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * <p>Provides authentication and authorization state as reactive signals for UI components.</p>
 *
 * <p>This class exposes session-scoped state derived from the Spring Security context,
 * including whether the current user is authenticated, has admin privileges, and is a
 * local user. UI components can subscribe to these signals to reactively adjust their
 * visibility and behavior.</p>
 *
 * <p>The state is updated explicitly via {@link #refreshFromSecurityContext()}, which
 * reads the current authentication information from the security layer.</p>
 */
@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationState {

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
        authenticated.set(isAuthenticated);
        admin.set(isAuthenticated && isAdmin);
        localUser.set(isAuthenticated && isLocalUser);
    }

    /**
     * <p>Returns the signal indicating whether the current user is authenticated.</p>
     *
     * <p>The signal emits {@code true} if the user is logged in, otherwise {@code false}.
     * UI components can subscribe to this signal to reactively update their state
     * based on authentication status.</p>
     *
     * @return The authentication signal; never {@code null}.
     */
    public ValueSignal<Boolean> getAuthenticatedSignal() {
        return authenticated;
    }

    /**
     * <p>Returns the signal indicating whether the current user has admin privileges.</p>
     *
     * <p>The signal emits {@code true} only if the user is authenticated and has
     * the admin role. Otherwise, it emits {@code false}. This ensures that admin
     * state is never {@code true} for unauthenticated users.</p>
     *
     * @return The admin signal; never {@code null}.
     */
    public ValueSignal<Boolean> getAdminSignal() {
        return admin;
    }

    /**
     * <p>Returns the signal indicating whether the current user is a local user.</p>
     *
     * <p>The signal emits {@code true} only if the user is authenticated and of type
     * local. Otherwise, it emits {@code false}. This can be used to distinguish local
     * users from remote or anonymous users in the UI.</p>
     *
     * @return The local user signal; never {@code null}.
     */
    public ValueSignal<Boolean> getLocalUserSignal() {
        return localUser;
    }

}
