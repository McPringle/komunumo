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
package app.komunumo.security;

import app.komunumo.data.dto.UserRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * <p>Utility to run code with a temporary technical authentication that
 * carries a specific {@link UserRole} authority.</p>
 *
 * <p>Use this for trusted internal tasks (bootstrap, demo seeding,
 * migrations, maintenance) that must call methods protected by method
 * security without weakening those policies.</p>
 *
 * <p>The current authentication is saved and restored to ensure no
 * elevated privileges leak beyond the scoped execution.</p>
 *
 * <p>Note: This does not bypass authorization. It sets a technical
 * principal that holds the requested authority so existing checks still
 * apply.</p>
 */
@Component
public final class SystemAuthenticator {

    /**
     * <p>Executes the given {@link Callable} while temporarily setting a
     * technical authentication that holds the given {@link UserRole}.</p>
     *
     * <p>The previous authentication is restored after execution. Checked
     * exceptions are wrapped in a {@link RuntimeException}.</p>
     *
     * @param role The role to grant during the action; must not be null.
     * @param action The work to execute with the given role; must not be null.
     * @return The result produced by the action, can be {@code null}.
     * @throws RuntimeException If the action throws an exception.
     */
    public <T> @Nullable T runAs(final @NotNull UserRole role,
                                 final @NotNull Callable<T> action) {
        final var context = SecurityContextHolder.getContext();
        final var previous = context.getAuthentication();
        try {
            context.setAuthentication(authFor(role));
            return action.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            context.setAuthentication(previous);
        }
    }

    /**
     * <p>Executes the given {@link Runnable} while temporarily setting a
     * technical authentication that holds the given {@link UserRole}.</p>
     *
     * <p>This is a convenience overload that delegates to
     * {@link #runAs(UserRole, Callable)}.</p>
     *
     * @param role The role to grant during the action; must not be null.
     * @param action The work to execute with the given role; must not be null.
     */
    public void runAs(final @NotNull UserRole role,
                      final @NotNull Runnable action) {
        runAs(role, () -> {
            action.run();
            return null;
        });
    }

    /**
     * <p>Executes the given {@link Callable} as an admin. This is a
     * convenience shortcut for {@code runAs(UserRole.ADMIN, action)}.</p>
     *
     * @param action The work to execute with admin privileges; must not be null.
     * @return The result produced by the action, can be {@code null}.
     * @throws RuntimeException If the action throws an exception.
     */
    public <T> @Nullable T runAsAdmin(final @NotNull Callable<T> action) {
        return runAs(UserRole.ADMIN, action);
    }

    /**
     * <p>Executes the given {@link Runnable} as an admin. This is a
     * convenience shortcut for {@code runAs(UserRole.ADMIN, action)}.</p>
     *
     * @param action The work to execute with admin privileges; must not be null.
     */
    public void runAsAdmin(final @NotNull Runnable action) {
        runAs(UserRole.ADMIN, action);
    }

    /**
     * <p>Executes the given {@link Callable} as a user. This is a
     * convenience shortcut for {@code runAs(UserRole.USER, action)}.</p>
     *
     * @param action The work to execute with user privileges; must not be null.
     * @return The result produced by the action, can be {@code null}.
     * @throws RuntimeException If the action throws an exception.
     */
    public <T> @Nullable T runAsUser(final @NotNull Callable<T> action) {
        return runAs(UserRole.USER, action);
    }

    /**
     * <p>Executes the given {@link Runnable} as a user. This is a
     * convenience shortcut for {@code runAs(UserRole.USER, action)}.</p>
     *
     * @param action The work to execute with user privileges; must not be null.
     */
    public void runAsUser(final @NotNull Runnable action) {
        runAs(UserRole.USER, action);
    }

    /**
     * <p>Creates a technical {@link Authentication} for the given role.</p>
     *
     * @param role The role to embed as authority; must not be null.
     * @return A technical authentication holding the given authority.
     */
    private @NotNull Authentication authFor(final @NotNull UserRole role) {
        return new UsernamePasswordAuthenticationToken(
                "system", "N/A",
                AuthorityUtils.createAuthorityList(role.getRole()));
    }

}
