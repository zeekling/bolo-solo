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
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.handler.Handler;
import org.b3log.solo.model.Option;
import org.b3log.solo.service.OptionQueryService;
import org.b3log.solo.util.Skins;
import org.b3log.solo.util.Solos;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skin handler for front-end skin switching.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.6.4
 */
public class SkinHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkinHandler.class);

    @Override
    public void handle(final RequestContext context) {
        final String skin = getSkinDirName(context);
        context.attr(Keys.TEMAPLTE_DIR_NAME, skin);

        context.handle();
    }

    private String getSkinDirName(final RequestContext context) {
        String skin = Skins.getSkinDirNameFromCookie(context.getRequest());

        if (StringUtils.isBlank(skin)) {
            final BeanManager beanManager = BeanManager.getInstance();
            final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
            final JSONObject skinOpt = optionQueryService.getSkin();

            if (Solos.isMobile(context.getRequest())) {
                if (null != skinOpt) {
                    skin = skinOpt.optString(Option.ID_C_MOBILE_SKIN_DIR_NAME);
                } else {
                    skin = Option.DefaultPreference.DEFAULT_MOBILE_SKIN_DIR_NAME;
                }
            } else {
                if (null != skinOpt) {
                    skin = skinOpt.optString(Option.ID_C_SKIN_DIR_NAME);
                } else {
                    skin = Option.DefaultPreference.DEFAULT_SKIN_DIR_NAME;
                }
            }
        }

        return skin;
    }
}
