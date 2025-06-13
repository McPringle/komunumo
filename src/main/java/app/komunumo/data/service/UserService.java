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

import app.komunumo.data.db.tables.records.UserRecord;
import app.komunumo.data.dto.UserDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static app.komunumo.data.db.tables.User.USER;

@Service
public final class UserService {

    private final @NotNull DSLContext dsl;
    private final @NotNull UniqueIdGenerator idGenerator;

    public UserService(final @NotNull DSLContext dsl,
                       final @NotNull UniqueIdGenerator idGenerator) {
        super();
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public @NotNull UserDto storeUser(final @NotNull UserDto user) {
        final UserRecord userRecord = dsl.fetchOptional(USER, USER.ID.eq(user.id()))
                .orElse(dsl.newRecord(USER));
        userRecord.from(user);

        if (userRecord.getId() == null) { // NOSONAR (false positive: ID may be null for new users)
            userRecord.setId(idGenerator.getUniqueID(USER));
        }

        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (userRecord.getCreated() == null) { // NOSONAR (false positive: date may be null for new users)
            userRecord.setCreated(now);
            userRecord.setUpdated(now);
        } else {
            userRecord.setUpdated(now);
        }
        userRecord.store();
        return userRecord.into(UserDto.class);
    }

    public int getAdminCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(USER)
                        .where(USER.ROLE.eq("admin"))
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public @NotNull Optional<UserDto> getUserByEmail(final @NotNull String email) {
        return dsl.selectFrom(USER)
                .where(USER.EMAIL.eq(email))
                .fetchOptionalInto(UserDto.class);
    }

    public boolean deleteUser(final @NotNull UserDto user) {
        return dsl.delete(USER)
                .where(USER.ID.eq(user.id()))
                .execute() > 0;
    }

}
