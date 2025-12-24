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
package app.komunumo.domain.user.control;

import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.domain.core.confirmation.entity.ConfirmationContext;
import app.komunumo.domain.core.confirmation.control.ConfirmationHandler;
import app.komunumo.domain.core.confirmation.entity.ConfirmationRequest;
import app.komunumo.domain.core.confirmation.entity.ConfirmationResponse;
import app.komunumo.domain.core.confirmation.control.ConfirmationService;
import app.komunumo.domain.core.confirmation.entity.ConfirmationStatus;
import app.komunumo.SecurityConfig;
import app.komunumo.domain.user.entity.UserPrincipal;
import app.komunumo.domain.core.i18n.controller.TranslationProvider;
import app.komunumo.domain.user.entity.AuthenticationSignal;
import app.komunumo.util.SecurityUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

@Service
public final class LoginService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    private static final @NotNull String CONTEXT_LOGIN_LOCATION = "location";

    private final @NotNull ObjectProvider<AuthenticationSignal> authenticationSignalProvider;

    private final @NotNull UserService userService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull TranslationProvider translationProvider;

    public LoginService(final @NotNull UserService userService,
                        final @NotNull ConfirmationService confirmationService,
                        final @NotNull TranslationProvider translationProvider,
                        final @NotNull ObjectProvider<AuthenticationSignal> authenticationSignalProvider) {
        super();
        this.userService = userService;
        this.confirmationService = confirmationService;
        this.translationProvider = translationProvider;
        this.authenticationSignalProvider = authenticationSignalProvider;
    }

    public boolean login(final @NotNull String emailAddress) {
        return internalLogin(emailAddress);
    }

    private boolean internalLogin(final @NotNull String emailAddress) {
        final var optUser = userService.getUserByEmail(emailAddress);
        if (optUser.isEmpty()) {
            LOGGER.info("User with email {} not found.", emailAddress);
            authenticationSignalProvider.ifAvailable(signal -> signal.setAuthenticated(false));
            return false;
        }

        final var user = optUser.orElseThrow();
        if (!user.type().isLoginAllowed()) {
            LOGGER.info("User with email {} exists but login is not allowed for type {}", emailAddress, user.type());
            authenticationSignalProvider.ifAvailable(signal -> signal.setAuthenticated(false));
            return false;
        }

        final var roles = new ArrayList<GrantedAuthority>();
        roles.add(new SimpleGrantedAuthority(UserRole.USER.getRole()));
        if (user.role().equals(UserRole.ADMIN)) {
            roles.add(new SimpleGrantedAuthority(UserRole.ADMIN.getRole()));
        }
        roles.add(new SimpleGrantedAuthority("ROLE_USER_" + user.type().name()));

        final var authorities = Collections.unmodifiableList(roles);
        final var principal = new UserPrincipal(user, authorities);


        // Authentication-Token without password (passwordless)
        final var authentication = new PreAuthenticatedAuthenticationToken(principal, null, authorities);

        // create and set SecurityContext
        final var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // persist in HTTP session
        final var request = VaadinService.getCurrentRequest();
        final var response = VaadinService.getCurrentResponse();
        if (request instanceof VaadinServletRequest vaadinServletRequest
                && response instanceof VaadinServletResponse vaadinServletResponse) {
            final var httpServletRequest = vaadinServletRequest.getHttpServletRequest();
            final var httpServletResponse = vaadinServletResponse.getHttpServletResponse();
            new HttpSessionSecurityContextRepository().saveContext(context, httpServletRequest, httpServletResponse);
        } else {
            // fallback: should never happen in Vaadin UI context
            LOGGER.warn("No Vaadin servlet request/response available; SecurityContext not saved to session.");
        }

        LOGGER.info("User with email {} successfully logged in.", emailAddress);
        authenticationSignalProvider.ifAvailable(signal ->
                signal.setAuthenticated(true, UserRole.ADMIN.equals(user.role()),
                        UserType.LOCAL.equals(user.type())));

        return true;
    }

    public @NotNull Optional<UserDto> getLoggedInUser() {
        return SecurityUtil.getUserPrincipal()
                .flatMap(principal -> userService.getUserById(principal.getUserId()));
    }

    public boolean isUserLoggedIn() {
        return getLoggedInUser().isPresent();
    }

    public void logout() {
        logout(SecurityConfig.LOGOUT_SUCCESS_URL);
    }

    public void logout(final @NotNull String location) {
        SecurityContextHolder.clearContext();
        final var logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
        authenticationSignalProvider.ifAvailable(signal -> signal.setAuthenticated(false));
        UI.getCurrentOrThrow().getPage().setLocation(location);
    }

    public void startLoginProcess(final @NotNull Locale locale,
                                  final @NotNull String location) {
        final var actionMessage = translationProvider.getTranslation("user.control.LoginService.actionText", locale);
        final ConfirmationHandler actionHandler = this::passwordlessLoginHandler;
        final var actionContext = ConfirmationContext.of(CONTEXT_LOGIN_LOCATION, location);
        final var confirmationRequest = new ConfirmationRequest(
                actionMessage,
                actionHandler,
                actionContext,
                locale
        );
        confirmationService.startConfirmationProcess(confirmationRequest);
    }

    private @NotNull ConfirmationResponse passwordlessLoginHandler(final @NotNull String email,
                                                                   final @NotNull ConfirmationContext context,
                                                                   final @NotNull Locale locale) {
        if (login(email)) {
            final var status = ConfirmationStatus.SUCCESS;
            final var message = translationProvider.getTranslation("user.control.LoginService.successMessage", locale);
            final var location = (String) context.getOrDefault(CONTEXT_LOGIN_LOCATION, "");
            return new ConfirmationResponse(status, message, location);
        }
        final var message = translationProvider.getTranslation("user.control.LoginService.failedMessage", locale);
        return new ConfirmationResponse(ConfirmationStatus.ERROR, message, "");
    }

}
