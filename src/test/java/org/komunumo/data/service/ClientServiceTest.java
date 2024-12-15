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

import org.junit.jupiter.api.Test;
import org.komunumo.data.entity.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @Test
    void happyCase() {
        // store a new client into the database
        var client = new Client(null, null, null, "Test Client");
        client = clientService.storeClient(client);
        assertEquals(1L, client.id());
        assertEquals("Test Client", client.name());
        assertNotNull(client.created());
        assertNotNull(client.updated());
        assertEquals(client.created(), client.updated());

        // read the client from the database
        client = clientService.getClient(1L).orElseThrow();
        assertEquals(1L, client.id());
        assertEquals("Test Client", client.name());
        assertNotNull(client.created());
        assertNotNull(client.updated());
        assertEquals(client.created(), client.updated());

        // update the existing client
        client = new Client(client.id(), client.created(), client.updated(), "Test Client Modified");
        client = clientService.storeClient(client);
        assertEquals(1L, client.id());
        assertEquals("Test Client Modified", client.name());
        assertNotNull(client.created());
        assertNotNull(client.updated());
        assertTrue(client.updated().isAfter(client.created()));

        // delete the existing client
        assertTrue(clientService.deleteClient(client));
        assertTrue(clientService.getClient(1L).isEmpty());

        // delete the non-existing client (was already deleted before)
        assertFalse(clientService.deleteClient(client));
    }

}
