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

public class AntiXSSTest {

  @Test
  public void shouldReturnNull_whenInputIsNull() {
    String result = AntiXSS.getSafeStringXSS(null);
    Assert.assertNull(result);
  }

  @Test
  public void shouldReturnEmpty_whenInputIsEmpty() {
    String result = AntiXSS.getSafeStringXSS("");
    Assert.assertEquals(result, "");
  }

  @Test
  public void shouldEscapeLessThanSign() {
    String result = AntiXSS.getSafeStringXSS("<");
    Assert.assertEquals(result, "&lt;");
  }

  @Test
  public void shouldEscapeGreaterThanSign() {
    String result = AntiXSS.getSafeStringXSS(">");
    Assert.assertEquals(result, "&gt;");
  }

  @Test
  public void shouldEscapeSingleQuote() {
    String result = AntiXSS.getSafeStringXSS("'");
    Assert.assertEquals(result, "&prime;");
  }

  @Test
  public void shouldEscapeDoubleQuote() {
    String result = AntiXSS.getSafeStringXSS("\"");
    Assert.assertEquals(result, "&quot;");
  }

  @Test
  public void shouldEscapeAmpersand() {
    String result = AntiXSS.getSafeStringXSS("&");
    Assert.assertEquals(result, "＆");
  }

  @Test
  public void shouldEscapeHash() {
    String result = AntiXSS.getSafeStringXSS("#");
    Assert.assertEquals(result, "＃");
  }

  @Test
  public void shouldEscapeBackslash() {
    String result = AntiXSS.getSafeStringXSS("\\");
    Assert.assertEquals(result, "￥");
  }

  @Test
  public void shouldEscapeEqualSign() {
    String result = AntiXSS.getSafeStringXSS("=");
    Assert.assertEquals(result, "&#61;");
  }
}
