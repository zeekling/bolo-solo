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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package org.b3log.solo.middleware;

import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.handler.Handler;
import org.b3log.latke.model.Role;
import org.b3log.solo.util.Solos;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * The admin auth check middleware for admin console.
 * <p>
 * Replaces ConsoleAdminAuthAdvice for Latke 3.x middleware pattern.
 * Checks if user is logged in as administrator.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.9.5
 */
public class ConsoleAdminAuthMidware implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleAdminAuthMidware.class);

    @Override
    public void handle(final RequestContext context) {
        final String requestURI = context.requestURI();
        final String contextPath = Latkes.getContextPath();

        // Only apply to /admin/* routes
        if (!requestURI.startsWith(contextPath + "/admin")) {
            context.handle();
            return;
        }

        if (!Solos.isAdminLoggedIn(context)) {
            LOGGER.debug("Unauthorized admin request [{}], admin not logged in", requestURI);
            context.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // User is admin, continue to next handler
        context.handle();
    }
}
