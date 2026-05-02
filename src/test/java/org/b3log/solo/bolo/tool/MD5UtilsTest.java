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
 * MD5Utils 测试类。
 *
 * @author <a href="https://github.com/bolo-blog">Bolo Team</a>
 */
public class MD5UtilsTest {

    @Test
    public void shouldReturnCorrectMD5_whenInputIsValid() {
        String result = MD5Utils.stringToMD5("hello");
        Assert.assertEquals(result, "5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    public void shouldReturnMD5With32Characters_whenInputIsValid() {
        String result = MD5Utils.stringToMD5("test");
        Assert.assertEquals(result.length(), 32);
    }

    @Test
    public void shouldReturnConsistentMD5_forSameInput() {
        String input = "password123";
        String result1 = MD5Utils.stringToMD5(input);
        String result2 = MD5Utils.stringToMD5(input);
        Assert.assertEquals(result1, result2);
    }

    @Test
    public void shouldReturnDifferentMD5_forDifferentInput() {
        String result1 = MD5Utils.stringToMD5("abc");
        String result2 = MD5Utils.stringToMD5("def");
        Assert.assertNotEquals(result1, result2);
    }

    @Test
    public void shouldReturnEmptyMD5_whenInputIsEmpty() {
        String result = MD5Utils.stringToMD5("");
        Assert.assertEquals(result, "d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    public void shouldReturnCorrectDoubleMD5() {
        String result = MD5Utils.stringToMD5Twice("hello");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.length(), 32);
    }
}