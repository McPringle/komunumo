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

import app.komunumo.data.db.tables.records.GlobalPageRecord;
import app.komunumo.data.dto.GlobalPageDto;
import app.komunumo.util.LocaleUtil;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.komunumo.data.db.tables.GlobalPage.GLOBAL_PAGE;

@Service
public final class GlobalPageService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(GlobalPageService.class);

    private final @NotNull DSLContext dsl;

    public GlobalPageService(final @NotNull DSLContext dsl) {
        super();
        this.dsl = dsl;
    }

    public GlobalPageDto storeGlobalPage(final @NotNull GlobalPageDto globalPage) {
        final var slot = globalPage.slot();
        final var languageCode = LocaleUtil.getLanguageCode(globalPage.language());
        final GlobalPageRecord globalPageRecord = dsl.selectFrom(GLOBAL_PAGE)
                .where(GLOBAL_PAGE.SLOT.eq(slot))
                .and(GLOBAL_PAGE.LANGUAGE.eq(languageCode))
                .fetchOptional()
                .orElse(dsl.newRecord(GLOBAL_PAGE));
        globalPageRecord.from(globalPage);
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (globalPageRecord.getCreated() == null) { // NOSONAR (false positive: date may be null for new global pages)
            globalPageRecord.setCreated(now);
            globalPageRecord.setUpdated(now);
        } else {
            globalPageRecord.setUpdated(now);
        }
        globalPageRecord.store();
        return globalPageRecord.into(GlobalPageDto.class);

    }

    public @NotNull Optional<GlobalPageDto> getGlobalPage(final @NotNull String slot,
                                                                   final @NotNull Locale locale) {
        final var languageCode = LocaleUtil.getLanguageCode(locale);
        final var globalPage = dsl.selectFrom(GLOBAL_PAGE)
                .where(GLOBAL_PAGE.SLOT.eq(slot))
                .and(GLOBAL_PAGE.LANGUAGE.eq(languageCode))
                .fetchOptionalInto(GlobalPageDto.class);

        if (globalPage.isEmpty() && !languageCode.equals("EN")) {
            return getGlobalPage(slot, Locale.ENGLISH);
        }

        return globalPage;
    }

    public @NotNull List<@NotNull GlobalPageDto> getGlobalPages(final @NotNull Locale locale) {
        final var preferredLanguageCode = LocaleUtil.getLanguageCode(locale);
        final var fallbackLanguageCode = "EN";

        // Load all pages in the desired language + fallback language
        final var pages = dsl.selectFrom(GLOBAL_PAGE)
                .where(GLOBAL_PAGE.LANGUAGE.in(preferredLanguageCode, fallbackLanguageCode))
                .fetchStreamInto(GlobalPageDto.class);

        // Keep only the page in the desired language per slot, or else fallback
        final var pageMap = pages.collect(Collectors.toMap(
                GlobalPageDto::slot,
                Function.identity(),
                (preferred, fallback) ->
                        preferred.language().equals(locale) ? preferred : fallback
        ));

        return pageMap.values().stream().toList();
    }

    public @NotNull List<@NotNull GlobalPageDto> getAllGlobalPages() {
        return dsl.selectFrom(GLOBAL_PAGE)
                .fetchInto(GlobalPageDto.class);
    }

    public int getGlobalPageCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(GLOBAL_PAGE)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public boolean updateGlobalPage(final @NotNull GlobalPageDto globalPage,
                                    final @NotNull String title,
                                    final @NotNull String markdown) {
        final var slot = globalPage.slot();
        final var languageCode = LocaleUtil.getLanguageCode(globalPage.language());
        return dsl.update(GLOBAL_PAGE)
                .set(GLOBAL_PAGE.TITLE, title)
                .set(GLOBAL_PAGE.MARKDOWN, markdown)
                .set(GLOBAL_PAGE.UPDATED, ZonedDateTime.now(ZoneOffset.UTC))
                .where(GLOBAL_PAGE.SLOT.eq(slot)
                        .and(GLOBAL_PAGE.LANGUAGE.eq(languageCode)))
                .execute() == 1;
    }

    public boolean deleteGlobalPage(final @NotNull GlobalPageDto globalPage) {
        final var slot = globalPage.slot();
        final var languageCode = LocaleUtil.getLanguageCode(globalPage.language());
        return dsl.delete(GLOBAL_PAGE)
                .where(GLOBAL_PAGE.SLOT.eq(slot))
                .and(GLOBAL_PAGE.LANGUAGE.eq(languageCode))
                .execute() > 0;
    }

}
