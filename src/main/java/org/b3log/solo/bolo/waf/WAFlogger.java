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
package org.b3log.solo.bolo.waf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>bolo-solo</h3>
 * <p>Logger of WAF.</p>
 *
 * @author : https://github.com/adlered
 * @date : 2020-05-31
 **/
public class WAFlogger {

    public static final String prefix = "[WAF] ";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WAFlogger.class);

    public static void log(String log) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(log);
        LOGGER.info(stringBuilder.toString());
    }

    public static void logTrace(String requestIP, String requestURL) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(requestIP + " accessed " + requestURL);
        LOGGER.info(stringBuilder.toString());
    }

    public static void logError(String log) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(log);
        LOGGER.error(stringBuilder.toString());
    }

    public static void logWarn(String log) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(log);
        LOGGER.warn(stringBuilder.toString());
    }
}
