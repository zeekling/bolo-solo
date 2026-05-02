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

public class ImagesTest {

  @Test
  public void shouldReturnUnchanged_whenNoImagesInHtml() {
    String html = "<p>Hello World</p>";
    String result = Images.qiniuImgProcessing(html);
    Assert.assertEquals(result, html);
  }

  @Test
  public void shouldNotModifyGifImages() {
    String html = "<img src=\"https://example.com/image.gif\">";
    String result = Images.qiniuImgProcessing(html);
    Assert.assertEquals(result, html);
  }

  @Test
  public void shouldProcessJpgImages() {
    String html = "<img src=\"https://example.com/photo.jpg\">";
    String result = Images.qiniuImgProcessing(html);
    String expected =
        "https://example.com/photo.jpg?imageView2/2/w/1280/format/jpg/interlace/1/q/100";
    Assert.assertTrue(result.contains(expected));
  }

  @Test
  public void shouldProcessPngImages() {
    String html = "<img src=\"https://example.com/photo.png\">";
    String result = Images.qiniuImgProcessing(html);
    String expected =
        "https://example.com/photo.png?imageView2/2/w/1280/format/jpg/interlace/1/q/100";
    Assert.assertTrue(result.contains(expected));
  }

  @Test
  public void shouldHandleMultipleImages() {
    String html = "<img src=\"https://example.com/1.jpg\"><img src=\"https://example.com/2.png\">";
    String result = Images.qiniuImgProcessing(html);
    Assert.assertTrue(result.contains("1.jpg?imageView2/2/w/1280/format/jpg/interlace/1/q/100"));
    Assert.assertTrue(result.contains("2.png?imageView2/2/w/1280/format/jpg/interlace/1/q/100"));
  }
}
