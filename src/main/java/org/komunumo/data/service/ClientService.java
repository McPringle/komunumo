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
import org.komunumo.data.db.tables.records.ClientRecord;
import org.komunumo.data.entity.Client;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.komunumo.data.db.Tables.CLIENT;

interface ClientService extends DSLContextGetter {

    @NotNull
    default Client storeClient(@NotNull final Client client) {
        final ClientRecord clientRecord = dsl().fetchOptional(CLIENT, CLIENT.ID.eq(client.id()))
                .orElse(dsl().newRecord(CLIENT));
        clientRecord.from(client);
        final var now = LocalDateTime.now(ZoneOffset.UTC);
        if (clientRecord.getCreated() == null) {
            clientRecord.setCreated(now);
            clientRecord.setUpdated(now);
        } else {
            clientRecord.setUpdated(now);
        }
        clientRecord.store();
        return clientRecord.into(Client.class);
    }

    @NotNull
    default Optional<Client> getClient(@NotNull final Long id) {
        return dsl().selectFrom(CLIENT)
                .where(CLIENT.ID.eq(id))
                .fetchOptionalInto(Client.class);
    }

    default boolean deleteClient(@NotNull final Client client) {
        return dsl().delete(CLIENT)
                .where(CLIENT.ID.eq(client.id()))
                .execute() > 0;
    }

}
