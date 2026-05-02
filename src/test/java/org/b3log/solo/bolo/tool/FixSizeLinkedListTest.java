/*
 * Bolo - A stable and beautiful blogging system based in Solo.
 * Copyright (c) 2020-present, https://github.com/bolo-blog
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.solo.bolo.tool;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * FixSizeLinkedList 测试类。
 *
 * @author <a href="https://github.com/bolo-blog">Bolo Team</a>
 */
public class FixSizeLinkedListTest {

    @Test
    public void shouldNotExceedCapacity() {
        FixSizeLinkedList<String> list = new FixSizeLinkedList<>(3);
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        Assert.assertEquals(list.size(), 3);
    }

    @Test
    public void shouldRemoveFirstElement_whenCapacityExceeded() {
        FixSizeLinkedList<String> list = new FixSizeLinkedList<>(3);
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        Assert.assertEquals(list.get(0), "B");
        Assert.assertEquals(list.get(1), "C");
        Assert.assertEquals(list.get(2), "D");
    }

    @Test
    public void shouldMaintainOrder() {
        FixSizeLinkedList<Integer> list = new FixSizeLinkedList<>(5);
        list.add(1);
        list.add(2);
        list.add(3);
        Assert.assertEquals((int) list.get(0), 1);
        Assert.assertEquals((int) list.get(1), 2);
        Assert.assertEquals((int) list.get(2), 3);
    }

    @Test
    public void shouldWorkWithEmptyCapacity() {
        FixSizeLinkedList<String> list = new FixSizeLinkedList<>(0);
        try {
            list.add("A");
        } catch (Exception e) {
        }
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void shouldWorkWithCapacityOne() {
        FixSizeLinkedList<String> list = new FixSizeLinkedList<>(1);
        list.add("A");
        list.add("B");
        list.add("C");
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0), "C");
    }
}