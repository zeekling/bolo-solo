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
package org.b3log.solo.handler;

import org.apache.commons.lang3.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Handler invoked before each request processing.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.6.4
 */
public class BeforeRequestHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeforeRequestHandler.class);

    @Override
    public void handle(final RequestContext context) {
        context.attr(Keys.HttpRequest.START_TIME_MILLIS, System.currentTimeMillis());

        final String requestURI = context.requestURI();
        Stopwatchs.start("Request Initialized [requestURI=" + requestURI + "]");

        fillBotAttrs(context);

        LOGGER.trace("Request [URI={}]", requestURI);
    }

    private void fillBotAttrs(final RequestContext context) {
        final String userAgentStr = context.getRequest().getHeader("User-Agent");
        final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
        BrowserType browserType = userAgent.getBrowser().getBrowserType();

        if (StringUtils.containsIgnoreCase(userAgentStr, "mobile")
                || StringUtils.containsIgnoreCase(userAgentStr, "MQQBrowser")
                || StringUtils.containsIgnoreCase(userAgentStr, "iphone")
                || StringUtils.containsIgnoreCase(userAgentStr, "MicroMessenger")
                || StringUtils.containsIgnoreCase(userAgentStr, "CFNetwork")
                || StringUtils.containsIgnoreCase(userAgentStr, "Android")) {
            browserType = BrowserType.MOBILE_BROWSER;
        } else if (StringUtils.containsIgnoreCase(userAgentStr, "Iframely")
                || StringUtils.containsIgnoreCase(userAgentStr, "Google")
                || StringUtils.containsIgnoreCase(userAgentStr, "BUbiNG")
                || StringUtils.containsIgnoreCase(userAgentStr, "ltx71")
                || StringUtils.containsIgnoreCase(userAgentStr, "py")) {
            browserType = BrowserType.ROBOT;
        }

        context.attr(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, BrowserType.ROBOT == browserType);
        context.attr(Keys.HttpRequest.IS_MOBILE_BOT, BrowserType.MOBILE_BROWSER == browserType);
    }
}
