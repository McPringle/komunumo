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

import app.komunumo.data.dto.UserDto;
import app.komunumo.data.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public final class AuthenticatedUser {

    private final UserService userService;

    public AuthenticatedUser(final @NotNull UserService userService) {
        this.userService = userService;
    }

    public Optional<UserDto> getLoggedInUser() {
        final var context = SecurityContextHolder.getContext();
        return Optional.ofNullable(context.getAuthentication())
                .filter(authentication -> !(authentication instanceof AnonymousAuthenticationToken))
                .flatMap(authentication -> userService.getUserByEmail(authentication.getName()));
    }

    public boolean isLoggedIn() {
        return getLoggedInUser().isPresent();
    }

    public void logout() {
        logout(SecurityConfig.LOGOUT_SUCCESS_URL);
    }

    public void logout(final @NotNull String location) {
        UI.getCurrent().getPage().setLocation(location);
        SecurityContextHolder.clearContext();
        final var logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }

}
