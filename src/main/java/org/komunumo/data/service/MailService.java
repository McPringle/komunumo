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
package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.dto.MailTemplate;
import org.komunumo.data.dto.MailTemplateId;
import org.komunumo.data.service.getter.ConfigurationGetter;
import org.komunumo.data.service.getter.DSLContextGetter;
import org.komunumo.data.service.getter.MailSenderGetter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.komunumo.data.db.tables.MailTemplate.MAIL_TEMPLATE;

@Service
public interface MailService extends ConfigurationGetter, DSLContextGetter, MailSenderGetter {

    default void sendMail(@NotNull final MailTemplateId mailTemplateId,
                          @NotNull final Locale locale,
                          @Nullable final Map<String, String> variables,
                          @NotNull final String... emailAddresses) {
        final var mailTemplate = getMailTemplate(mailTemplateId, locale).orElseThrow();
        final var message = new SimpleMailMessage();
        message.setTo(emailAddresses);
        message.setFrom(appConfig().mail().from());
        final var replyTo = appConfig().mail().replyTo();
        if (!replyTo.isBlank()) {
            message.setReplyTo(replyTo);
        }
        message.setSubject(replaceVariables(mailTemplate.subject(), variables));
        message.setText(replaceVariables(mailTemplate.markdown(), variables));
        mailSender().send(message);
    }

    private String replaceVariables(@NotNull final String text,
                                    @Nullable final Map<String, String> variables) {
        String returnValue = text;
        if (variables != null) {
            for (final var entry : variables.entrySet()) {
                final var regex = Pattern.quote("${%s}".formatted(entry.getKey()));
                final var value = Matcher.quoteReplacement(variables.get(entry.getKey()));
                returnValue = returnValue.replaceAll(regex, value);
            }
        }
        return returnValue;
    }

    default @NotNull Optional<MailTemplate> getMailTemplate(final @NotNull MailTemplateId mailTemplateId,
                                                            final @NotNull Locale locale) {
        final var language = locale.getLanguage().toUpperCase(locale);
        return dsl().selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.ID.eq(mailTemplateId.name()))
                .and(MAIL_TEMPLATE.LANGUAGE.eq(language))
                .fetchOptionalInto(MailTemplate.class);
    }

}
