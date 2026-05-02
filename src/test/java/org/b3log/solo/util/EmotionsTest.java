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
package org.b3log.solo.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EmotionsTest {

  @Test
  public void shouldReturnUnchanged_whenNoEmojiInContent() {
    String content = "Hello World! This is a plain text without any emoji.";
    String result = Emotions.convert(content);
    Assert.assertEquals(result, content);
  }

  @Test
  public void shouldConvertEmojiToAlias() {
    String content = "I am happy :smile: today!";
    String result = Emotions.convert(content);
    Assert.assertNotEquals(result, content);
  }
}
