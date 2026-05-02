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
package org.b3log.solo;

import org.b3log.latke.ioc.BeanManager;
import org.mockito.Mockito;
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
            throw new RuntimeException("Failed to set mock BeanManager", e);
        }
    }
}