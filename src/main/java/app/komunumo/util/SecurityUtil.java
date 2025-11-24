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
package app.komunumo.util;

import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.domain.user.entity.UserPrincipal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * <p>Utility class for checking authentication and authorization state
 * of the current user within the Spring Security context.</p>
 *
 * <p>This class uses Spring Security's {@link SecurityContextHolder} to
 * evaluate the current authentication. It is designed to be used in
 * Vaadin views (including {@code @AnonymousAllowed} views) to decide
 * whether the user is logged in or has specific roles.</p>
 *
 * <p><b>Note:</b> The checks are thread-local, i.e., evaluated per request
 * and per user. Concurrent users will each see their own authentication
 * state.</p>
 */
public final class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * <p>Returns the current {@link Authentication} object if the user is authenticated.</p>
     *
     * @return an {@link Authentication} object or {@code null} if the user is not authenticated
     */
    private static @Nullable Authentication getAuthentication() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication : null;
    }

    /**
     * <p>Returns the current {@link UserPrincipal} if the user is authenticated.</p>
     *
     * <p>Anonymous users and non-{@code UserPrincipal} principals yield an empty Optional.</p>
     *
     * @return an {@link Optional} containing the current {@link UserPrincipal} if available
     */
    public static @NotNull Optional<UserPrincipal> getUserPrincipal() {
        final var authentication = getAuthentication();
        if (authentication != null) {
            final var principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal) {
                return Optional.of((UserPrincipal) principal);
            }
        }
        return Optional.empty();
    }

    /**
     * <p>Checks whether the current user is authenticated (i.e., has a {@link UserPrincipal}).</p>
     *
     * @return {@code true} if the user is logged in and not anonymous, {@code false} otherwise
     */
    public static boolean isLoggedIn() {
        return getUserPrincipal().isPresent();
    }

    /**
     * <p>Checks whether the current user has the given role.</p>
     *
     * <p>This method inspects the authorities of the current authentication and compares
     * against the Spring-conventional {@code ROLE_} prefix.</p>
     *
     * @param role the role to check (e.g., {@link UserRole#ADMIN})
     * @return {@code true} if the current user has the role, {@code false} otherwise
     */
    public static boolean hasRole(final @NotNull UserRole role) {
        final var authentication = getAuthentication();
        if (authentication != null) {
            final var roleName = role.getRole();
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> roleName.equals(authority.getAuthority()));
        }
        return false;
    }

    /**
     * <p>Checks if the current user is of the provided type.</p>
     *
     * <p>The method checks if the authenticated user principal is of the type that is provided to the method.</p>
     *
     * @param type the type to check the user principal against (e.g., {@link UserType#LOCAL})
     * @return {@code true} if the current user is of the type, else return {@code false}
     */
    public static boolean isUserType(final @NotNull UserType type) {
        return getUserPrincipal()
                .filter(principal -> type.equals(principal.getType()))
                .isPresent();
    }

    /**
     * <p>Convenience method to check whether the current user has the {@link UserRole#ADMIN} role.</p>
     *
     * @return {@code true} if the current user is an administrator, {@code false} otherwise
     */
    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * <p>Method to check if the current user has the {@link UserRole#USER} and is of type {@link UserType#LOCAL}.</p>
     *
     * @return {@code true} if the current user is a local user, else return {@code false}
     */
    public static boolean isLocalUser() {
        return hasRole(UserRole.USER) && isUserType(UserType.LOCAL);
    }
}
