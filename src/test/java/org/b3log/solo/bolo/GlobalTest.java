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
package org.b3log.solo.bolo;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Global 测试类。
 *
 * @author <a href="https://github.com/bolo-blog">Bolo Team</a>
 */
public class GlobalTest {

  @Test
  public void shouldHaveCorrectHacpaiDomain() {
    Assert.assertEquals(Global.HACPAI_DOMAIN, "ld246.com");
  }

  @Test
  public void shouldHaveCorrectFishPiDomain() {
    Assert.assertEquals(Global.FISH_PI_DOMAIN, "fishpi.cn");
  }

  @Test
  public void shouldHaveNonEmptyDomains() {
    Assert.assertNotNull(Global.HACPAI_DOMAIN);
    Assert.assertNotNull(Global.FISH_PI_DOMAIN);
    Assert.assertTrue(Global.HACPAI_DOMAIN.length() > 0);
    Assert.assertTrue(Global.FISH_PI_DOMAIN.length() > 0);
  }
}
