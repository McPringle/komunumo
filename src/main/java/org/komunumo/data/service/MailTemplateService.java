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
import org.komunumo.data.service.getter.DSLContextGetter;

import java.util.Locale;

import static org.komunumo.data.db.tables.MailTemplate.MAIL_TEMPLATE;

public interface MailTemplateService extends DSLContextGetter {

    default @Nullable MailTemplate getMailTemplate(final @NotNull MailTemplateId mailTemplateId,
                                                   final @NotNull Locale locale) {
        final var language = locale.getLanguage().toUpperCase(locale);
        return dsl().selectFrom(MAIL_TEMPLATE)
                .where(MAIL_TEMPLATE.ID.eq(mailTemplateId.name()))
                .and(MAIL_TEMPLATE.LANGUAGE.eq(language))
                .fetchOneInto(MailTemplate.class);
    }

}
