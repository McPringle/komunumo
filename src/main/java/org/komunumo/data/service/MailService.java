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

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.komunumo.configuration.AppConfig;
import org.komunumo.data.dto.MailFormat;
import org.komunumo.data.dto.MailTemplate;
import org.komunumo.data.dto.MailTemplateId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.komunumo.data.db.tables.MailTemplate.MAIL_TEMPLATE;
import static org.komunumo.data.dto.MailFormat.HTML;
import static org.komunumo.data.dto.MailFormat.MARKDOWN;

@Service
public final class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final @NotNull AppConfig appConfig;
    private final @NotNull JavaMailSender mailSender;
    private final @NotNull DSLContext dsl;

    private final @NotNull Parser markdownParser;
    private final @NotNull HtmlRenderer htmlRenderer;

    public MailService(final @NotNull AppConfig appConfig,
                       final @NotNull JavaMailSender mailSender,
                       final @NotNull DSLContext dsl) {
        this.appConfig = appConfig;
        this.mailSender = mailSender;
        this.dsl = dsl;

        final var options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        markdownParser = Parser.builder(options).build();
        htmlRenderer = HtmlRenderer.builder(options).build();
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
                    subject, emailAddresses, e.getMessage(), e);
            return false;
        }
    }

    private String convertMarkdownToHtml(final @NotNull String markdown) {
        final var document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    private @NotNull String replaceVariables(final @NotNull String text,
                                    final @Nullable Map<String, String> variables) {
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

    public @NotNull Optional<@NotNull MailTemplate> getMailTemplate(final @NotNull MailTemplateId mailTemplateId,
                                                            final @NotNull Locale locale) {
        final var language = locale.getLanguage().toUpperCase(locale);
        return dsl.selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.ID.eq(mailTemplateId.name()))
                .and(MAIL_TEMPLATE.LANGUAGE.eq(language))
                .fetchOptionalInto(MailTemplate.class);
    }

}
