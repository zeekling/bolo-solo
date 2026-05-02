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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * DeleteFolder 测试类。
 *
 * @author <a href="https://github.com/bolo-blog">Bolo Team</a>
 */
public class DeleteFolderTest {

  private File tempDir;

  @BeforeMethod
  public void setUp() throws IOException {
    tempDir = Files.createTempDirectory("DeleteFolderTest_").toFile();
  }

  @AfterMethod
  public void tearDown() {
    if (tempDir != null && tempDir.exists()) {
      deleteRecursively(tempDir);
    }
  }

  private void deleteRecursively(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    file.delete();
  }

  @Test
  public void shouldReturnFalse_whenPathDoesNotExist() {
    String nonExistentPath = tempDir.getAbsolutePath() + "/non_existent_folder";
    boolean result = DeleteFolder.delAllFile(nonExistentPath);
    Assert.assertFalse(result, "路径不存在时应该返回 false");
  }

  @Test
  public void shouldDeleteEmptyFolder() {
    File emptyFolder = new File(tempDir, "emptyFolder");
    emptyFolder.mkdirs();
    Assert.assertTrue(emptyFolder.exists(), "空文件夹应该创建成功");

    DeleteFolder.delFolder(emptyFolder.getAbsolutePath());
    Assert.assertFalse(emptyFolder.exists(), "空文件夹应该被删除");
  }

  @Test
  public void shouldDeleteFilesInFolder() throws IOException {
    File folderWithFiles = new File(tempDir, "folderWithFiles");
    folderWithFiles.mkdirs();

    File file1 = new File(folderWithFiles, "test1.txt");
    File file2 = new File(folderWithFiles, "test2.txt");
    file1.createNewFile();
    file2.createNewFile();

    Assert.assertTrue(file1.exists(), "文件1应该创建成功");
    Assert.assertTrue(file2.exists(), "文件2应该创建成功");

    boolean result = DeleteFolder.delAllFile(folderWithFiles.getAbsolutePath());
    Assert.assertFalse(file1.exists(), "文件1应该被删除");
    Assert.assertFalse(file2.exists(), "文件2应该被删除");
  }

  @Test
  public void shouldDeleteNestedFolders() throws IOException {
    File nestedFolder = new File(tempDir, "level1");
    File level2 = new File(nestedFolder, "level2");
    File level3 = new File(level2, "level3");
    level3.mkdirs();

    File deepFile = new File(level3, "deepFile.txt");
    deepFile.createNewFile();

    Assert.assertTrue(nestedFolder.exists(), "嵌套文件夹应该存在");
    Assert.assertTrue(deepFile.exists(), "深层文件应该存在");

    DeleteFolder.delFolder(nestedFolder.getAbsolutePath());
    Assert.assertFalse(nestedFolder.exists(), "嵌套文件夹应该被完全删除");
  }
}
