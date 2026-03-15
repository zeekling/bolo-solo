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
import org.b3log.latke.model.User;
import org.b3log.solo.util.Solos;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

/**
 * The common auth check middleware for admin console.
 * <p>
 * Replaces ConsoleAuthAdvice for Latke 3.x middleware pattern.
 * Checks if user is logged in and is not a visitor role.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.9.5
 */
public class ConsoleAuthMidware implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleAuthMidware.class);

    @Override
    public void handle(final RequestContext context) {
        final String requestURI = context.requestURI();
        final String contextPath = Latkes.getContextPath();

        // Only apply to /console/* routes (except public ones)
        if (!requestURI.startsWith(contextPath + "/console")) {
            context.handle();
            return;
        }

        // Skip authentication for public console routes
        if (isPublicRoute(requestURI, contextPath)) {
            context.handle();
            return;
        }

        final JSONObject currentUser = Solos.getCurrentUser(context.getRequest(), context.getResponse());
        if (null == currentUser) {
            LOGGER.debug("Unauthorized request [{}], user not logged in", requestURI);
            context.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String userRole = currentUser.optString(User.USER_ROLE);
        if (Role.VISITOR_ROLE.equals(userRole)) {
            LOGGER.debug("Forbidden request [{}], user is visitor", requestURI);
            context.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // User is authenticated, continue to next handler
        context.handle();
    }

    private boolean isPublicRoute(final String requestURI, final String contextPath) {
        // Public routes that don't require authentication
        return requestURI.startsWith(contextPath + "/console/markdown/2html")
                || requestURI.startsWith(contextPath + "/console/article-pwd")
                || requestURI.equals(contextPath + "/console/changeRole")
                || requestURI.startsWith(contextPath + "/console/export");
    }
}
