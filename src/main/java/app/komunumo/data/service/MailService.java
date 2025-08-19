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

import app.komunumo.configuration.AppConfig;
import app.komunumo.data.dto.MailFormat;
import app.komunumo.data.dto.MailTemplate;
import app.komunumo.data.dto.MailTemplateId;
import app.komunumo.util.LocaleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static app.komunumo.data.db.tables.MailTemplate.MAIL_TEMPLATE;
import static app.komunumo.data.dto.MailFormat.HTML;
import static app.komunumo.data.dto.MailFormat.MARKDOWN;
import static app.komunumo.util.MarkdownUtil.convertMarkdownToHtml;
import static app.komunumo.util.TemplateUtil.replaceVariables;

@Service
public final class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final @NotNull AppConfig appConfig;
    private final @NotNull JavaMailSender mailSender;
    private final @NotNull DSLContext dsl;

    public MailService(final @NotNull AppConfig appConfig,
                       final @NotNull JavaMailSender mailSender,
                       final @NotNull DSLContext dsl) {
        this.appConfig = appConfig;
        this.mailSender = mailSender;
        this.dsl = dsl;
    }

    public boolean sendMail(final @NotNull MailTemplateId mailTemplateId,
                            final @NotNull Locale locale,
                            final @NotNull MailFormat format,
                            final @Nullable Map<String, String> variables,
                            final @NotNull String... emailAddresses) {
        final var mailTemplate = getMailTemplate(mailTemplateId, locale).orElseThrow();
        final var subject = replaceVariables(mailTemplate.subject(), variables);
        final var markdown = replaceVariables(mailTemplate.markdown(), variables);

        try {
            final var mimeMessage = mailSender.createMimeMessage();
            final var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            helper.setTo(emailAddresses);
            helper.setFrom(appConfig.mail().from());

            final var replyTo = appConfig.mail().replyTo();
            if (!replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }

            helper.setSubject(subject);

            final var body = format == MARKDOWN
                    ? markdown
                    : convertMarkdownToHtml(markdown);
            helper.setText(body, format == HTML);

            mailSender.send(mimeMessage);

            LOGGER.info("Mail with subject '{}' successfully sent to {}",
                    subject, emailAddresses);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Unable to send mail with subject '{}' to {}: {}",
                    subject, emailAddresses, e.getMessage());
            return false;
        }
    }

    public @NotNull Optional<MailTemplate> getMailTemplate(final @NotNull MailTemplateId mailTemplateId,
                                                            final @NotNull Locale locale) {
        final var languageCode = LocaleUtil.getLanguageCode(locale);
        return dsl.selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.ID.eq(mailTemplateId.name()))
                .and(MAIL_TEMPLATE.LANGUAGE.eq(languageCode))
                .fetchOptionalInto(MailTemplate.class);
    }

}
