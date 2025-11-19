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
package app.komunumo.business.user.control;

import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.business.user.entity.UserDto;
import app.komunumo.business.user.entity.UserRole;
import app.komunumo.business.user.entity.UserType;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.data.service.MailService;
import app.komunumo.business.core.confirmation.entity.ConfirmationContext;
import app.komunumo.business.core.confirmation.control.ConfirmationHandler;
import app.komunumo.business.core.confirmation.entity.ConfirmationRequest;
import app.komunumo.business.core.confirmation.entity.ConfirmationResponse;
import app.komunumo.business.core.confirmation.control.ConfirmationService;
import app.komunumo.business.core.confirmation.entity.ConfirmationStatus;
import app.komunumo.business.core.i18n.controller.TranslationProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_REGISTRATION_ALLOWED;

@Service
public final class RegistrationService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);
    private static final @NotNull String CONTEXT_REGISTRATION_LOCATION = "location";

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull UserService userService;
    private final @NotNull LoginService loginService;
    private final @NotNull MailService mailService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull TranslationProvider translationProvider;

    public RegistrationService(final @NotNull ConfigurationService configurationService,
                               final @NotNull UserService userService,
                               final @NotNull LoginService loginService,
                               final @NotNull MailService mailService,
                               final @NotNull ConfirmationService confirmationService,
                               final @NotNull TranslationProvider translationProvider) {
        super();
        this.configurationService = configurationService;
        this.userService = userService;
        this.loginService = loginService;
        this.mailService = mailService;
        this.confirmationService = confirmationService;
        this.translationProvider = translationProvider;
    }


    public void startRegistrationProcess(final @NotNull Locale locale,
                                         final @NotNull String location) {
        if (!configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class)) {
            LOGGER.warn("Registration attempt while registration is disabled.");
            return;
        }

        final var actionMessage = translationProvider.getTranslation("data.service.AccountService.registrationText", locale);
        final ConfirmationHandler actionHandler = this::passwordlessRegistrationHandler;
        final var actionContext = ConfirmationContext.of(CONTEXT_REGISTRATION_LOCATION, location);
        final var confirmationRequest = new ConfirmationRequest(
                actionMessage,
                actionHandler,
                actionContext,
                locale
        );
        confirmationService.startConfirmationProcess(confirmationRequest);
    }

    private @NotNull ConfirmationResponse passwordlessRegistrationHandler(final @NotNull String email,
                                                                          final @NotNull ConfirmationContext context,
                                                                          final @NotNull Locale locale) {
        var localUser = userService.getUserByEmail(email).orElseGet(() -> createNewLocalUser(email));

        if (localUser.type() != UserType.LOCAL) {
            localUser = userService.changeUserType(localUser, UserType.LOCAL);
            userService.storeUser(localUser);
        }

        mailService.sendMail(MailTemplateId.ACCOUNT_REGISTRATION_SUCCESS, locale, MailFormat.MARKDOWN, Map.of(), email);
        loginService.login(email);

        final var status = ConfirmationStatus.SUCCESS;
        final var message = translationProvider.getTranslation("data.service.AccountService.successMessage", locale);
        final var location = (String) context.getOrDefault(CONTEXT_REGISTRATION_LOCATION, "");
        return new ConfirmationResponse(status, message, location);
    }

    private UserDto createNewLocalUser(final @NotNull String email) {
        return userService.storeUser(new UserDto(null, null, null, null, email, "", "", null,
                UserRole.USER, UserType.LOCAL));
    }

}
