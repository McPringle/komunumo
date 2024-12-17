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
import org.komunumo.data.db.tables.records.GroupRecord;
import org.komunumo.data.entity.Group;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.komunumo.data.db.Tables.GROUP;

interface GroupService extends DSLContextGetter {

    @NotNull
    default Group storeGroup(@NotNull final Group group) {
        final GroupRecord groupRecord = dsl().fetchOptional(GROUP, GROUP.ID.eq(group.id()))
                .orElse(dsl().newRecord(GROUP));
        groupRecord.from(group);
        final var now = LocalDateTime.now(ZoneOffset.UTC);
        if (groupRecord.getCreated() == null) {
            groupRecord.setCreated(now);
            groupRecord.setUpdated(now);
        } else {
            groupRecord.setUpdated(now);
        }
        groupRecord.store();
        return groupRecord.into(Group.class);
    }

    @NotNull
    default Optional<Group> getGroup(@NotNull final Long id) {
        return dsl().selectFrom(GROUP)
                .where(GROUP.ID.eq(id))
                .fetchOptionalInto(Group.class);
    }

    default boolean deleteGroup(@NotNull final Group group) {
        return dsl().delete(GROUP)
                .where(GROUP.ID.eq(group.id()))
                .execute() > 0;
    }

}
