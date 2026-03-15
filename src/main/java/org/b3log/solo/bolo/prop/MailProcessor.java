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
package org.b3log.solo.bolo.prop;

import io.github.biezhi.ome.OhMyEmail;
import io.github.biezhi.ome.SendMailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.b3log.latke.http.RequestContext;
import org.b3log.solo.util.Solos;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h3>bolo-solo</h3>
 * <p>属性 API.</p>
 *
 * @author : https://github.com/adlered
 * @date : 2019-12-20 20:02
 **/
@Singleton
public class MailProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailProcessor.class);

    /**
     * 发送普通邮件
     *
     * @param subject
     * @param from
     * @param to
     * @param html
     * @throws SendMailException
     */
    public static void localSendMailMethod(String subject, String from, String to, String html) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OhMyEmail.subject(subject)
                            .from(from)
                            .to(to)
                            .html(html)
                            .send();
                    LOGGER.info("Mail has sent [subject=" + subject + ", from=" + from + ", to=" + to + ", html=" + html + "]");
                } catch (SendMailException SME) {
                    LOGGER.info("Mail sent failed [cause=" + SME.getCause() + ", subject=" + subject + ", from=" + from + ", to=" + to + ", html=" + html + "]");
                }
            }
        }).start();
    }

    /*
        === 静态方法区 ===
     */
    public void sendMail(final RequestContext context) {
        if (!Solos.isAdminLoggedIn(context)) {
            context.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        HttpServletRequest request = context.getRequest();
        String subject = request.getParameter("subject");
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String html = request.getParameter("html");

        try {
            OhMyEmail.subject(subject)
                    .from(from)
                    .to(to)
                    .html(html)
                    .send();

            context.renderJSON().renderCode(200);
            context.renderJSON().renderMsg("Mail has sent.");
            LOGGER.info("Mail has sent [subject=" + subject + ", from=" + from + ", to=" + to + ", html=" + html + "]");

            return;
        } catch (SendMailException SME) {
            LOGGER.error("Send mail failed! Please check your MailBox Settings.");

            context.renderJSON().renderCode(500);
            context.renderJSON().renderMsg("Send mail failed! Please check your MailBox Settings.");

            return;
        }
    }
}
