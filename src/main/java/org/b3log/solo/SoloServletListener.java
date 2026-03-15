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

import java.util.Locale;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;

import org.apache.commons.lang3.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.BeanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.b3log.latke.plugin.PluginManager;
import org.b3log.latke.plugin.ViewLoadEventHandler;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.solo.bolo.prop.MailService;
import org.b3log.solo.bolo.waf.WAF;
import org.b3log.solo.event.B3ArticleSender;
import org.b3log.solo.event.B3ArticleUpdater;
import org.b3log.solo.event.DeleteArticleListener;
import org.b3log.solo.event.DeleteFollowListener;
import org.b3log.solo.event.FishPiArticleSender;
import org.b3log.solo.event.FishPiArticleUpdater;
import org.b3log.solo.event.FollowArticleRefresher;
import org.b3log.solo.event.PluginRefresher;
import org.b3log.solo.model.Option;
import org.b3log.solo.processor.InitCheckHandler;
import org.b3log.solo.processor.KanBanNiangProcessor;
import org.b3log.solo.processor.PermalinkHandler;
import org.b3log.solo.processor.console.AdminConsole;
import org.b3log.solo.processor.console.ArticleConsole;
import org.b3log.solo.processor.console.CategoryConsole;
import org.b3log.solo.processor.console.CommentConsole;
import org.b3log.solo.processor.console.FollowConsole;
import org.b3log.solo.processor.console.LinkConsole;
import org.b3log.solo.processor.console.OtherConsole;
import org.b3log.solo.processor.console.PageConsole;
import org.b3log.solo.processor.console.PluginConsole;
import org.b3log.solo.processor.console.PreferenceConsole;
import org.b3log.solo.processor.console.RepairConsole;
import org.b3log.solo.processor.console.SkinConsole;
import org.b3log.solo.processor.console.TagConsole;
import org.b3log.solo.processor.console.UserConsole;
import org.b3log.solo.repository.OptionRepository;
import org.b3log.solo.service.CronMgmtService;
import org.b3log.solo.service.ImportService;
import org.b3log.solo.service.InitService;
import org.b3log.solo.service.OptionQueryService;
import org.b3log.solo.service.SkinMgmtService;
import org.b3log.solo.service.StatisticMgmtService;
import org.b3log.solo.service.UpgradeService;
import org.b3log.solo.util.Markdowns;
import org.b3log.solo.util.Skins;
import org.b3log.solo.util.Solos;
import org.json.JSONException;
import org.json.JSONObject;

import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Solo Servlet listener.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @author <a href="http://vanessa.b3log.org">Vanessa</a>
 * @since 0.3.1
 */
public final class SoloServletListener extends AbstractServletListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoloServletListener.class);

    /**
     * Bolo version.
     */
    public static final String STABLE_CN = "稳定版";
    public static final String STABLE_EN = "Stable";
    public static final String BETA_CN = "内测版";
    public static final String BETA_EN = "Beta";
    public static final String BOLO_VERSION_SOURCE = "2.6.4";
    public static final String BOLO_VERSION = "v" + BOLO_VERSION_SOURCE + " " + STABLE_CN;
    public static final String BOLO_VERSION_EN = "v" + BOLO_VERSION_SOURCE + " " + STABLE_EN;
    // public static final String BOLO_VERSION = "v" + BOLO_VERSION_SOURCE + " " +
    // BETA_CN;
    // public static final String BOLO_VERSION_EN = "v" + BOLO_VERSION_SOURCE + " "
    // + BETA_EN;
    /**
     * Solo version.
     */
    public static String VERSION = "4.3.4";

    /**
     * Bean manager.
     */
    private BeanManager beanManager;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Latkes.setScanPath("org.b3log.solo");
        super.contextInitialized(servletContextEvent);
        Dispatcher.HANDLERS.add(0, new InitCheckHandler());
        Dispatcher.HANDLERS.add(1, new PermalinkHandler());

        beanManager = BeanManager.getInstance();
        routeConsoleProcessors();
        Stopwatchs.start("Context Initialized");

        validateSkin();

        final InitService initService = beanManager.getReference(InitService.class);
        initService.initTables();

        if (initService.isInited()) {
            // Upgrade check https://github.com/b3log/solo/issues/12040
            final UpgradeService upgradeService = beanManager.getReference(UpgradeService.class);
            upgradeService.upgrade();

            // Import check https://github.com/b3log/solo/issues/12293
            final ImportService importService = beanManager.getReference(ImportService.class);
            importService.importMarkdowns();

            final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);
            final Transaction transaction = optionRepository.beginTransaction();
            try {
                loadPreference();

                if (transaction.isActive()) {
                    transaction.commit();
                }
            } catch (final Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        }

        registerEventHandlers();

        final PluginManager pluginManager = beanManager.getReference(PluginManager.class);
        pluginManager.load();

        String header = "" +
                "█████████████████████████████████████████████████████████████████████████\n" +
                "█                                      █                                █\n" +
                "█  ██████╗  ██████╗ ██╗      ██████╗   █                                █\n" +
                "█  ██╔══██╗██╔═══██╗██║     ██╔═══██╗  █      Welcome to Bolo :)        █\n" +
                "█  ██████╔╝██║   ██║██║     ██║   ██║  █                                █\n" +
                "█  ██╔══██╗██║   ██║██║     ██║   ██║  █ github.com/bolo-blog/bolo-solo █\n" +
                "█  ██████╔╝╚██████╔╝███████╗╚██████╔╝  █  CurrentVersion: " + BOLO_VERSION_EN + " █\n" +
                "█  ╚═════╝  ╚═════╝ ╚══════╝ ╚═════╝   █                                █\n" +
                "█                                      █                                █\n" +
                "████✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩█████\n" +
                "█               THANK YOU FOR YOUR CONTRIBUTION TO BOLO !               █\n" +
                "████✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩✩█████\n" +
                "█     adlered    (Author)         █    https://github.com/adlered       █\n" +
                "█     expoli     (Contributor)    █    https://github.com/expoli        █\n" +
                "█     zeekling   (Contributor)    █    https://github.com/zeekling      █\n" +
                "█     csfwff     (Contributor)    █    https://github.com/csfwff        █\n" +
                "█     teahouse   (Contributor)    █    https://github.com/teahouse15    █\n" +
                "█     Gakkiyomi  (Contributor)    █    https://github.com/gakkiyomi     █\n" +
                "█████████████████████████████████████████████████████████████████████████\n" +
                " \n" +
                "┌\n" +
                "├　　HTTP Server Running On:　" + Latkes.getServePath() + "\n" +
                "├　　JVM Memory:　" + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024
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

        Stopwatchs.end();
        LOGGER.debug("Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());

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
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        final CronMgmtService cronMgmtService = beanManager.getReference(CronMgmtService.class);
        cronMgmtService.stop();

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
        super.sessionDestroyed(httpSessionEvent);
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();
        Requests.log(httpServletRequest, Level.DEBUG, LOGGER);

        final String requestURI = httpServletRequest.getRequestURI();
        Stopwatchs.start("Request Initialized [requestURI=" + requestURI + "]");
        fillBotAttrs(httpServletRequest);
        if (!Solos.isBot(httpServletRequest)) {
            final StatisticMgmtService statisticMgmtService = beanManager.getReference(StatisticMgmtService.class);
            statisticMgmtService.onlineVisitorCount(httpServletRequest);
        }

        resolveSkinDir(httpServletRequest);
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        Stopwatchs.end();

        LOGGER.debug("Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
        Stopwatchs.release();

        super.requestDestroyed(servletRequestEvent);
    }

    /**
     * Loads skin.
     * <p>
     * Loads skin from repository, loads skins from skin directory then sets it into
     * preference if the skins changed.
     * </p>
     */
    private void loadPreference() {
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

            final String showClodeBlockLn = preference.optString(org.b3log.solo.model.Option.ID_C_SHOW_CODE_BLOCK_LN);
            Markdowns.SHOW_CODE_BLOCK_LN = StringUtils.equalsIgnoreCase(showClodeBlockLn, "true");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);

            System.exit(-1);
        }

        Stopwatchs.end();
    }

    /**
     * Register event handlers.
     */
    private void registerEventHandlers() {
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
            final FollowArticleRefresher followArticleRefresher = beanManager
                    .getReference(FollowArticleRefresher.class);
            eventManager.registerListener(followArticleRefresher);
            final DeleteFollowListener deleteFollowListener = beanManager
                    .getReference(DeleteFollowListener.class);
            eventManager.registerListener(deleteFollowListener);
        } catch (final Exception e) {
            LOGGER.error("Register event handlers failed", e);

            System.exit(-1);
        }

        LOGGER.debug("Registered event handlers");
        Stopwatchs.end();
    }

    /**
     * Resolve skin (template) for the specified HTTP servlet request.
     * 前台皮肤切换 https://github.com/b3log/solo/issues/12060
     *
     * @param httpServletRequest the specified HTTP servlet request
     */
    private void resolveSkinDir(final HttpServletRequest httpServletRequest) {
        String skin = Skins.getSkinDirNameFromCookie(httpServletRequest);
        if (StringUtils.isBlank(skin)) {
            final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
            final JSONObject skinOpt = optionQueryService.getSkin();
            if (Solos.isMobile(httpServletRequest)) {
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

        httpServletRequest.setAttribute(Keys.TEMAPLTE_DIR_NAME, skin);
    }

    private static void fillBotAttrs(final HttpServletRequest request) {
        final String userAgentStr = request.getHeader("User-Agent");
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

        request.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, BrowserType.ROBOT == browserType);
        request.setAttribute(Keys.HttpRequest.IS_MOBILE_BOT, BrowserType.MOBILE_BROWSER == browserType);
    }

    /**
     * Validates the default skin.
     *
     * <p>
     * 改进皮肤加载校验 https://github.com/b3log/solo/issues/12548
     * </p>
     */
    private static void validateSkin() {
        final String skinDirName = Option.DefaultPreference.DEFAULT_SKIN_DIR_NAME;
        final String skinName = Latkes.getSkinName(skinDirName);
        if (StringUtils.isBlank(skinName)) {
            LOGGER.error("Can't load the default skins, please make sure skin [" + skinDirName
                    + "] is under skins directory and structure correctly");

            System.exit(-1);
        }
    }

    /**
     * 后台控制器使用函数式路由. https://github.com/b3log/solo/issues/12580
     */
    public static void routeConsoleProcessors() {
        final BeanManager beanManager = BeanManager.getInstance();
        final AdminConsole adminConsole = beanManager.getReference(AdminConsole.class);
        Dispatcher.get("/admin-index.do", adminConsole::showAdminIndex);
        Dispatcher.get("/admin-preference.do", adminConsole::showAdminPreferenceFunction);
        Dispatcher.route().get(new String[] { "/admin-article.do",
                "/admin-article-list.do",
                "/admin-comment-list.do",
                "/admin-link-list.do",
                "/admin-follow-list.do",
                "/admin-page-list.do",
                "/admin-others.do",
                "/admin-draft-list.do",
                "/admin-user-list.do",
                "/admin-category-list.do",
                "/admin-theme-list.do",
                "/admin-plugin-list.do",
                "/admin-main.do",
                "/admin-about.do",
                "/admin-tool-box.do",
                "/admin-usite.do" }, adminConsole::showAdminFunctions);
        Dispatcher.get("/console/export/sql", adminConsole::exportSQL);
        Dispatcher.get("/console/export/json", adminConsole::exportJSON);
        Dispatcher.get("/console/export/hexo", adminConsole::exportHexo);

        final ArticleConsole articleConsole = beanManager.getReference(ArticleConsole.class);
        Dispatcher.get("/console/article/push2rhy", articleConsole::pushArticleToCommunity);
        Dispatcher.get("/console/thumbs", articleConsole::getArticleThumbs);
        Dispatcher.get("/console/article/{id}", articleConsole::getArticle);
        Dispatcher.get("/console/articles/status/{status}/{page}/{pageSize}/{windowSize}",
                articleConsole::getArticles);
        Dispatcher.delete("/console/article/{id}", articleConsole::removeArticle);
        Dispatcher.put("/console/article/unpublish/{id}", articleConsole::cancelPublishArticle);
        Dispatcher.put("/console/article/canceltop/{id}", articleConsole::cancelTopArticle);
        Dispatcher.put("/console/article/puttop/{id}", articleConsole::putTopArticle);
        Dispatcher.put("/console/article/", articleConsole::updateArticle);
        Dispatcher.post("/console/article/", articleConsole::addArticle);

        final CategoryConsole categoryConsole = beanManager.getReference(CategoryConsole.class);
        Dispatcher.put("/console/category/order/", categoryConsole::changeOrder);
        Dispatcher.get("/console/category/{id}", categoryConsole::getCategory);
        Dispatcher.delete("/console/category/{id}", categoryConsole::removeCategory);
        Dispatcher.put("/console/category/", categoryConsole::updateCategory);
        Dispatcher.post("/console/category/", categoryConsole::addCategory);
        Dispatcher.get("/console/categories/{page}/{pageSize}/{windowSize}", categoryConsole::getCategories);

        final CommentConsole commentConsole = beanManager.getReference(CommentConsole.class);
        Dispatcher.delete("/console/article/comment/{id}", commentConsole::removeArticleComment);
        Dispatcher.get("/console/comments/{page}/{pageSize}/{windowSize}", commentConsole::getComments);
        Dispatcher.get("/console/comments/article/{id}", commentConsole::getArticleComments);

        final LinkConsole linkConsole = beanManager.getReference(LinkConsole.class);
        Dispatcher.delete("/console/link/{id}", linkConsole::removeLink);
        Dispatcher.put("/console/link/", linkConsole::updateLink);
        Dispatcher.put("/console/link/order/", linkConsole::changeOrder);
        Dispatcher.post("/console/link/", linkConsole::addLink);
        Dispatcher.get("/console/links/{page}/{pageSize}/{windowSize}", linkConsole::getLinks);
        Dispatcher.get("/console/link/{id}", linkConsole::getLink);

        final FollowConsole followConsole = beanManager.getReference(FollowConsole.class);
        Dispatcher.delete("/console/follow/{id}", followConsole::removeFollow);
        Dispatcher.put("/console/follow/", followConsole::updateFollow);
        Dispatcher.put("/console/follow/order/", followConsole::changeOrder);
        Dispatcher.post("/console/follow/", followConsole::addFollow);
        Dispatcher.get("/console/follows/{page}/{pageSize}/{windowSize}", followConsole::getFollows);
        Dispatcher.get("/console/follow/{id}", followConsole::getFollow);

        final PageConsole pageConsole = beanManager.getReference(PageConsole.class);
        Dispatcher.put("/console/page/", pageConsole::updatePage);
        Dispatcher.delete("/console/page/{id}", pageConsole::removePage);
        Dispatcher.post("/console/page/", pageConsole::addPage);
        Dispatcher.put("/console/page/order/", pageConsole::changeOrder);
        Dispatcher.get("/console/page/{id}", pageConsole::getPage);
        Dispatcher.get("/console/pages/{page}/{pageSize}/{windowSize}", pageConsole::getPages);

        final PluginConsole pluginConsole = beanManager.getReference(PluginConsole.class);
        Dispatcher.put("/console/plugin/status/", pluginConsole::setPluginStatus);
        Dispatcher.get("/console/plugins/{page}/{pageSize}/{windowSize}", pluginConsole::getPlugins);
        Dispatcher.post("/console/plugin/toSetting", pluginConsole::toSetting);
        Dispatcher.post("/console/plugin/updateSetting", pluginConsole::updateSetting);

        final PreferenceConsole preferenceConsole = beanManager.getReference(PreferenceConsole.class);
        Dispatcher.get("/console/signs/", preferenceConsole::getSigns);
        Dispatcher.get("/console/preference/", preferenceConsole::getPreference);
        Dispatcher.put("/console/preference/", preferenceConsole::updatePreference);

        final SkinConsole skinConsole = beanManager.getReference(SkinConsole.class);
        Dispatcher.get("/console/skin", skinConsole::getSkin);
        Dispatcher.put("/console/skin", skinConsole::updateSkin);

        final RepairConsole repairConsole = beanManager.getReference(RepairConsole.class);
        Dispatcher.get("/fix/restore-signs", repairConsole::restoreSigns);

        final TagConsole tagConsole = beanManager.getReference(TagConsole.class);
        Dispatcher.get("/console/tags", tagConsole::getTags);
        Dispatcher.get("/console/tag/unused", tagConsole::getUnusedTags);

        final OtherConsole otherConsole = beanManager.getReference(OtherConsole.class);
        Dispatcher.delete("/console/archive/unused", otherConsole::removeUnusedArchives);
        Dispatcher.delete("/console/tag/unused", otherConsole::removeUnusedTags);

        final UserConsole userConsole = beanManager.getReference(UserConsole.class);
        Dispatcher.put("/console/user/", userConsole::updateUser);
        Dispatcher.delete("/console/user/{id}", userConsole::removeUser);
        Dispatcher.get("/console/users/{page}/{pageSize}/{windowSize}", userConsole::getUsers);
        Dispatcher.get("/console/user/{id}", userConsole::getUser);
        Dispatcher.get("/console/changeRole/{id}", userConsole::changeUserRole);

        Dispatcher.mapping();
    }
}
