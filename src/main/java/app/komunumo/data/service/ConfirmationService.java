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
package app.komunumo.data.service;

import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplateId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_URL;

@Service
public final class ConfirmationService {

    private static final @NotNull String CONFIRMATION_PATH = "/confirm";
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ConfirmationService.class);

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull MailService mailService;

    private final @NotNull Cache<@NotNull String, @NotNull ConfirmationData> confirmationCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(1_000) // prevent memory overflow (DDOS attack)
            .build();

    public ConfirmationService(final @NotNull ConfigurationService configurationService,
                               final @NotNull MailService mailService) {
        super();
        this.configurationService = configurationService;
        this.mailService = mailService;
    }

    public void startConfirmationProcess(final @NotNull String emailAddress,
                                         final @NotNull String confirmationReason,
                                         final @NotNull String onSuccessMessage,
                                         final @NotNull Runnable onSuccessHandler,
                                         final @NotNull Locale locale) {
        final var confirmationId = UUID.randomUUID().toString();
        final var confirmationData = new ConfirmationData(confirmationId, emailAddress,
                onSuccessMessage, onSuccessHandler, locale);
        confirmationCache.put(confirmationId, confirmationData);

        final var confirmationLink = generateConfirmationLink(confirmationData);
        final var instanceName = configurationService.getConfiguration(INSTANCE_NAME, locale);
        final Map<String, String> variables = Map.of(
                "instanceName", instanceName,
                "confirmationLink", confirmationLink,
                "confirmationReason", confirmationReason);
        mailService.sendMail(MailTemplateId.CONFIRMATION_PROCESS, locale, MailFormat.MARKDOWN, variables, emailAddress);
    }

    private String generateConfirmationLink(final @NotNull ConfirmationData confirmationData) {
        final var instanceUrl = configurationService.getConfiguration(INSTANCE_URL, confirmationData.locale());
        return UriComponentsBuilder
                .fromUriString(instanceUrl)
                .path(CONFIRMATION_PATH)
                .pathSegment(confirmationData.id().toString())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    public @NotNull Optional<String> confirm(final @NotNull String confirmationId) {
        final var confirmationData = confirmationCache.getIfPresent(confirmationId);
        if (confirmationData != null) {
            try {
                confirmationData.onSuccessHandler().run();
                confirmationCache.invalidate(confirmationId);
                return Optional.of(confirmationData.successMessage);
            } catch (final Exception exception) {
                LOGGER.error("Error in 'onSuccessHandler' for confirmation ID {}: {}",
                        confirmationId, exception.getMessage(), exception);
            }
        }
        return Optional.empty();
    }

    private record ConfirmationData(
            @NotNull String id,
            @NotNull String emailAddress,
            @NotNull String successMessage,
            @NotNull Runnable onSuccessHandler,
            @NotNull Locale locale
    ) { }

}
