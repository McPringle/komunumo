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
import org.komunumo.data.entity.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Test
    void happyCase() {
        // store a new group into the database
        var group = new Group(null, null, null, "Test Group");
        group = groupService.storeGroup(group);
        assertEquals(1L, group.id());
        assertEquals("Test Group", group.name());
        assertNotNull(group.created());
        assertNotNull(group.updated());
        assertEquals(group.created(), group.updated());

        // read the group from the database
        group = groupService.getGroup(1L).orElseThrow();
        assertEquals(1L, group.id());
        assertEquals("Test Group", group.name());
        assertNotNull(group.created());
        assertNotNull(group.updated());
        assertEquals(group.created(), group.updated());

        // update the existing group
        group = new Group(group.id(), group.created(), group.updated(), "Test Group Modified");
        group = groupService.storeGroup(group);
        assertEquals(1L, group.id());
        assertEquals("Test Group Modified", group.name());
        assertNotNull(group.created());
        assertNotNull(group.updated());
        assertTrue(group.updated().isAfter(group.created()));

        // delete the existing group
        assertTrue(groupService.deleteGroup(group));
        assertTrue(groupService.getGroup(1L).isEmpty());

        // delete the non-existing group (was already deleted before)
        assertFalse(groupService.deleteGroup(group));
    }

}
