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
package org.b3log.solo;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.plugin.PluginManager;
import org.b3log.latke.plugin.ViewLoadEventHandler;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.solo.bolo.prop.MailService;
import org.b3log.solo.bolo.waf.WAF;
import org.b3log.solo.event.*;
import org.b3log.solo.handler.AfterRequestHandler;
import org.b3log.solo.handler.BeforeRequestHandler;
import org.b3log.solo.handler.SkinHandler;
import org.b3log.solo.model.Option;
import org.b3log.solo.processor.InitCheckHandler;
import org.b3log.solo.processor.KanBanNiangProcessor;
import org.b3log.solo.processor.PermalinkHandler;
import org.b3log.solo.repository.OptionRepository;
import org.b3log.solo.service.*;
import org.b3log.solo.util.Markdowns;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Bolo Server for Latke 3.x functional routing.
 * This class replaces SoloServletListener for standalone server mode.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.6.4
 */
public final class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static final String VERSION = SoloServletListener.BOLO_VERSION;

    public static void main(final String[] args) {
        System.setProperty("https.protocols", "TLSv1.2");

        Latkes.setScanPath("org.b3log.solo");
        Latkes.init();

        routeProcessors();

        final BeanManager beanManager = BeanManager.getInstance();
        Stopwatchs.start("Context Initialized");

        validateSkin();

        final InitService initService = beanManager.getReference(InitService.class);
        initService.initTables();

        if (initService.isInited()) {
            final UpgradeService upgradeService = beanManager.getReference(UpgradeService.class);
            upgradeService.upgrade();

            final ImportService importService = beanManager.getReference(ImportService.class);
            importService.importMarkdowns();

            final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);
            final Transaction transaction = optionRepository.beginTransaction();
            try {
                loadPreference(beanManager);

                if (transaction.isActive()) {
                    transaction.commit();
                }
            } catch (final Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        }

        registerEventHandlers(beanManager);

        final PluginManager pluginManager = beanManager.getReference(PluginManager.class);
        pluginManager.load();

        printBanner();

        Stopwatchs.end();
        LOGGER.debug("Stopwatch: {}{}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());

        final CronMgmtService cronMgmtService = beanManager.getReference(CronMgmtService.class);
        cronMgmtService.start();

        MailService.loadMailSettings();
        WAF.set();
        new Thread(KanBanNiangProcessor::downloadKBNResource).start();

        try {
            final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
            final JSONObject preference = optionQueryService.getPreference();
            final String localeString = preference.getString(Option.ID_C_LOCALE_STRING);
            Latkes.setLocale(new Locale(Locales.getLanguage(localeString), Locales.getCountry(localeString)));
        } catch (JSONException ignored) {
        }

        final String portStr = Latkes.getServerPort();
        final int port = StringUtils.isNotBlank(portStr) ? Integer.parseInt(portStr) : 8080;
        final org.b3log.latke.Server server = new org.b3log.latke.Server();
        try {
            server.start(port);
            LOGGER.info("Bolo Server started on port {}", port);
        } catch (final Exception e) {
            LOGGER.error("Server start failed", e);
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                cronMgmtService.stop();
                server.stop();
                LOGGER.info("Bolo Server stopped");
            } catch (final Exception e) {
                LOGGER.error("Server stop failed", e);
            }
        }));
    }

    public static void routeProcessors() {
        Dispatcher.startRequestHandler = new BeforeRequestHandler();
        Dispatcher.HANDLERS.add(1, new SkinHandler());
        Dispatcher.HANDLERS.add(2, new InitCheckHandler());
        Dispatcher.HANDLERS.add(3, new PermalinkHandler());
        Dispatcher.endRequestHandler = new AfterRequestHandler();

        SoloServletListener.routeConsoleProcessors();
    }

    private static void validateSkin() {
        final String skinDirName = Option.DefaultPreference.DEFAULT_SKIN_DIR_NAME;
        final String skinName = Latkes.getSkinName(skinDirName);
        if (StringUtils.isBlank(skinName)) {
            LOGGER.error("Can't load the default skins, please make sure skin [" + skinDirName
                    + "] is under skins directory and structure correctly");
            System.exit(-1);
        }
    }

    private static void loadPreference(final BeanManager beanManager) {
        Stopwatchs.start("Load Preference");
        LOGGER.debug("Loading preference....");

        final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
        JSONObject skin;
        try {
            skin = optionQueryService.getSkin();
            if (null == skin) {
                return;
            }

            final SkinMgmtService skinMgmtService = beanManager.getReference(SkinMgmtService.class);
            skinMgmtService.loadSkins(skin);

            final JSONObject preference = optionQueryService.getPreference();
            if (null == preference) {
                return;
            }

            final String showCodeBlockLn = preference.optString(Option.ID_C_SHOW_CODE_BLOCK_LN);
            Markdowns.SHOW_CODE_BLOCK_LN = StringUtils.equalsIgnoreCase(showCodeBlockLn, "true");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-1);
        }

        Stopwatchs.end();
    }

    private static void registerEventHandlers(final BeanManager beanManager) {
        Stopwatchs.start("Register Event Handlers");
        LOGGER.debug("Registering event handlers....");

        try {
            final EventManager eventManager = beanManager.getReference(EventManager.class);
            final PluginRefresher pluginRefresher = beanManager.getReference(PluginRefresher.class);
            eventManager.registerListener(pluginRefresher);
            eventManager.registerListener(new ViewLoadEventHandler());
            final B3ArticleSender articleSender = beanManager.getReference(B3ArticleSender.class);
            eventManager.registerListener(articleSender);
            final B3ArticleUpdater articleUpdater = beanManager.getReference(B3ArticleUpdater.class);
            eventManager.registerListener(articleUpdater);
            final FishPiArticleSender fishPiArticleSender = beanManager.getReference(FishPiArticleSender.class);
            eventManager.registerListener(fishPiArticleSender);
            final FishPiArticleUpdater fishPiArticleUpdater = beanManager.getReference(FishPiArticleUpdater.class);
            eventManager.registerListener(fishPiArticleUpdater);
            final DeleteArticleListener deleteArticleListener = beanManager.getReference(DeleteArticleListener.class);
            eventManager.registerListener(deleteArticleListener);
            final FollowArticleRefresher followArticleRefresher = beanManager.getReference(FollowArticleRefresher.class);
            eventManager.registerListener(followArticleRefresher);
            final DeleteFollowListener deleteFollowListener = beanManager.getReference(DeleteFollowListener.class);
            eventManager.registerListener(deleteFollowListener);
        } catch (final Exception e) {
            LOGGER.error("Register event handlers failed", e);
            System.exit(-1);
        }

        LOGGER.debug("Registered event handlers");
        Stopwatchs.end();
    }

    private static void printBanner() {
        String header = "" +
                "█████████████████████████████████████████████████████████████████████████\n" +
                "█                                                                       █\n" +
                "█ ██████╗ ██████╗ ██╗   ██████╗ █                                      █\n" +
                "█ ██╔══██╗██╔═══██╗██║   ██╔═══██╗ █   Welcome to Bolo :)               █\n" +
                "█ ██████╔╝██║   ██║██║   ██║   ██║ █                                      █\n" +
                "█ ██╔══██╗██║   ██║██║   ██║   ██║ █   github.com/bolo-blog/bolo-solo   █\n" +
                "█ ██████╔╝╚██████╔╝███████╗╚██████╔╝ █   CurrentVersion: " + SoloServletListener.BOLO_VERSION_EN + "         █\n" +
                "█ ╚═════╝  ╚═════╝ ╚══════╝ ╚═════╝  █                                      █\n" +
                "█                                                                       █\n" +
                "████✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩█████\n" +
                "█ THANK YOU FOR YOUR CONTRIBUTION TO BOLO !                            █\n" +
                "████✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩█████\n" +
                "█ adlered (Author)         █ https://github.com/adlered               █\n" +
                "█ expoli (Contributor)     █ https://github.com/expoli                █\n" +
                "█ zeekling (Contributor)   █ https://github.com/zeekling              █\n" +
                "█ csfwff (Contributor)     █ https://github.com/csfwff                █\n" +
                "█ teahouse (Contributor)   █ https://github.com/teahouse15            █\n" +
                "█ Gakkiyomi (Contributor)  █ https://github.com/gakkiyomi             █\n" +
                "█████████████████████████████████████████████████████████████████████████\n" +
                " \n" +
                "┌\n" +
                "├ HTTP Server Running On: " + Latkes.getServePath() + "\n" +
                "├ JVM Memory: " + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024
                + "MB / " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB\n" +
                "└";
        System.out.println("");
        String[] lines = header.split("\n");
        for (String line : lines) {
            System.out.println(line);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("");
    }
}
