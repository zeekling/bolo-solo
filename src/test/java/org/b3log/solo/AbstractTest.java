/*
 * Bolo - 一个基于 Solo 的稳定美丽的博客系统。
 * Copyright (c) 2020-present, https://github.com/bolo-blog
 *
 * 本程序是自由软件：您可以 redistribute 它并根据 GNU Affero General Public License 的条款
 * 修改它，要么按照版本 3，要么（根据您的选择）任何后来的版本。
 *
 * 本程序旨在有用，但 WITHOUT ANY WARRANTY。
 * 您应该收到了 GNU Affero General Public License 的副本
 * 如果没有，请参阅 <https://www.gnu.org/licenses/>。
 */
package org.b3log.solo;

import org.b3log.latke.ioc.BeanManager;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract test base class.
 *
 * @author <a href="https://github.com/bolo-blog">Bolo Team</a>
 */
public abstract class AbstractTest {

    protected BeanManager mockBeanManager;

    @BeforeMethod
    public void setUp() {
        mockBeanManager = Mockito.mock(BeanManager.class);
        // 由于 BeanManager 是 final 类，使用反射设置 mock
        try {
            var field = BeanManager.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, mockBeanManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock BeanManager: instance field not accessible", e);
        }
    }

    @AfterMethod
    public void tearDown() {
        try {
            var field = BeanManager.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clean up mock BeanManager: instance field not accessible", e);
        }
    }
}