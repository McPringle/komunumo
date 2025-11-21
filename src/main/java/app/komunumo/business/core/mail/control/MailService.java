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
package app.komunumo.business.core.mail.control;

import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.business.core.config.entity.AppConfig;
import app.komunumo.business.core.mail.entity.MailFormat;
import app.komunumo.business.core.mail.entity.MailTemplate;
import app.komunumo.business.core.mail.entity.MailTemplateId;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static app.komunumo.data.db.tables.MailTemplate.MAIL_TEMPLATE;
import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_URL;
import static app.komunumo.business.core.mail.entity.MailFormat.HTML;
import static app.komunumo.business.core.mail.entity.MailFormat.MARKDOWN;
import static app.komunumo.util.MarkdownUtil.convertMarkdownToHtml;
import static app.komunumo.util.TemplateUtil.replaceVariables;

@Service
public final class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final @NotNull AppConfig appConfig;
    private final @NotNull ConfigurationService configurationService;
    private final @NotNull JavaMailSender mailSender;
    private final @NotNull DSLContext dsl;

    public MailService(final @NotNull AppConfig appConfig,
                       final @NotNull ConfigurationService configurationService,
                       final @NotNull JavaMailSender mailSender,
                       final @NotNull DSLContext dsl) {
        this.appConfig = appConfig;
        this.configurationService = configurationService;
        this.mailSender = mailSender;
        this.dsl = dsl;
    }

    public boolean sendMail(final @NotNull MailTemplateId mailTemplateId,
                            final @NotNull Locale locale,
                            final @NotNull MailFormat format,
                            final @Nullable Map<String, String> variables,
                            final @NotNull String... emailAddresses) {
        final var instanceName = configurationService.getConfiguration(INSTANCE_NAME);
        final var instanceUrl = configurationService.getConfiguration(INSTANCE_URL);
        final HashMap<String, String> allVariables = new HashMap<>();
        if (variables != null) {
            allVariables.putAll(variables);
        }
        allVariables.put("instanceName", instanceName);
        allVariables.put("instanceUrl", instanceUrl);

        final var mailTemplate = getMailTemplate(mailTemplateId, locale).orElseThrow();
        final var subject = "[%s] %s".formatted(instanceName, replaceVariables(mailTemplate.subject(), allVariables));
        final var markdown = replaceVariables(mailTemplate.markdown(), allVariables);

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
