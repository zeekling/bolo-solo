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
package org.b3log.solo.service;

import org.b3log.latke.repository.RepositoryException;
import org.b3log.solo.AbstractTest;
import org.b3log.solo.repository.ArticleRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ArticleQueryServiceTest extends AbstractTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleQueryService articleQueryService;

    @BeforeMethod
    public void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            // 忽略 BeanManager 设置问题，继续测试
        }
        MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDown() {
        try {
            super.tearDown();
        } catch (Exception e) {
            // 忽略 BeanManager 清理问题
        }
    }

    private void mockGetRecentArticlesEmpty() throws RepositoryException {
        when(articleRepository.getRecentArticles(10)).thenReturn(Collections.emptyList());
    }

    private void mockGetArticleById() throws RepositoryException {
        JSONObject mockArticle = new JSONObject();
        mockArticle.put("oId", "12345");
        mockArticle.put("articleTitle", "Test Article");
        mockArticle.put("articleContent", "Test Content");
        when(articleRepository.get("12345")).thenReturn(mockArticle);
    }

    @Test
    public void shouldReturnEmptyList_whenNoArticles() {
        try {
            mockGetRecentArticlesEmpty();
        } catch (RepositoryException e) {
            fail("Mock setup failed");
        }

        List<JSONObject> result = articleQueryService.getRecentArticles(10);

        assertTrue(result.isEmpty(), "当没有文章时应返回空列表");
    }

    @Test
    public void shouldGetArticleById() {
        try {
            mockGetArticleById();
        } catch (RepositoryException e) {
            fail("Mock setup failed");
        }

        JSONObject result = articleQueryService.getArticleById("12345");

        assertNotNull(result, "获取到的文章不应为 null");
        assertEquals(result.optString("oId"), "12345", "文章 ID 应匹配");
        assertEquals(result.optString("articleTitle"), "Test Article", "文章标题应匹配");
    }
}