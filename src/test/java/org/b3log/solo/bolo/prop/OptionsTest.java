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
package org.b3log.solo.bolo.prop;

import java.util.List;
import org.b3log.solo.model.Option;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionsTest {

  @Test
  public void shouldLoadOptions() {
    List<String> optionList = Options.loadOptions();
    Assert.assertNotNull(optionList);
    Assert.assertTrue(optionList.contains(Option.ID_C_BLOG_TITLE));
    Assert.assertTrue(optionList.contains(Option.ID_C_MAIL_BOX));
  }

  @Test
  public void shouldLoadDefaultOptions() {
    JSONObject requestJSON = new JSONObject();
    requestJSON.put("userName", "testuser");

    List<Object[]> optList = Options.loadOptList(requestJSON);

    Assert.assertNotNull(optList);
    Assert.assertTrue(optList.size() > 0);
  }

  @Test
  public void shouldReturnDefaultBlogTitle() {
    JSONObject requestJSON = new JSONObject();
    requestJSON.put("userName", "testuser");

    List<Object[]> optList = Options.loadOptList(requestJSON);

    boolean found = false;
    for (Object[] opt : optList) {
      if (Option.ID_C_BLOG_TITLE.equals(opt[0])) {
        Assert.assertEquals(opt[2], "testuser 的个人博客");
        found = true;
        break;
      }
    }
    Assert.assertTrue(found, "Should find blog title option");
  }
}
