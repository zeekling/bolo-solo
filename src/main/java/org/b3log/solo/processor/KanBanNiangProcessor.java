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
package org.b3log.solo.processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.RandomUtils;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.JsonRenderer;
import org.b3log.solo.SoloServletListener;
import org.b3log.solo.bolo.SslUtils;
import org.b3log.solo.model.Option;
import org.b3log.solo.repository.OptionRepository;
import org.b3log.solo.repository.PluginRepository;
import org.b3log.solo.util.Solos;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Iterator;

/**
 * KanBanNiang processor. https://github.com/b3log/solo/issues/12472
 *
 * @author <a href="http://88250.b3log.org">Liang Ding (Solo Author)</a>
 * @author <a href="https://github.com/adlered">adlered (Bolo Author)</a>
 * @since 2.9.2
 */
@Singleton
public class KanBanNiangProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KanBanNiangProcessor.class);

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Online KanBanNiang resources download.
     */
    public static void downloadKBNResource() {
        boolean enabled = true;
        try {
            final BeanManager beanManager = BeanManager.getInstance();
            final PluginRepository pluginRepository = beanManager.getReference(PluginRepository.class);
            final Transaction transaction = pluginRepository.beginTransaction();
            enabled = pluginRepository.get("看板娘 ＋_0.0.2").optString("status").equals("ENABLED");
            transaction.commit();
        } catch (Exception e) {
        }
        if (enabled) {
            String path = "";
            File file = null;
            try {
                LOGGER.info("KanBanNiang downloading ...");
                final ServletContext servletContext = SoloServletListener.getServletContext();
                final String assets = "/plugins/kanbanniang/assets/";
                path = servletContext.getResource(assets).getPath();
                path = URLDecoder.decode(path);
                String downloadURL = "https://ftp.stackoverflow.wiki/bolo/kanbanniang/KBNModel.zip";
                file = new File(path + "KBNModel.zip");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                URL url = new URL(downloadURL);
                SslUtils.ignoreSsl();
                URLConnection connection = url.openConnection();
                float size = (connection.getContentLength() / 1024 / 1024);
                System.out.println("KanBanNiang resource total size: " + size + " MB ");
                for (int i = 0; i <= (Math.round(size)); i++) {
                    String formatSize = String.valueOf(i);
                    if (formatSize.length() == 1) {
                        formatSize = "0" + formatSize;
                    }
                    System.out.print(formatSize + "MB ");
                }
                System.out.println();
                InputStream inputStream = connection.getInputStream();
                long sizeKB = 0;
                int length = 0;
                byte[] bytes = new byte[1024];
                while ((length = inputStream.read(bytes)) != -1) {
                    sizeKB++;
                    fileOutputStream.write(bytes, 0, length);
                    if (sizeKB % 205 == 0) {
                        System.out.print("▉");
                        Thread.sleep(128);
                    } else if (sizeKB == connection.getContentLength() / 1024) {
                        System.out.println(" OK");
                    }
                }
                LOGGER.info("Unpacking KanBanNiang ...");
                fileOutputStream.close();
                inputStream.close();
                ZipUtil.unpack(file, new File(path));
                file.delete();
                LOGGER.info("KanBanNiang is ready.");
            } catch (Exception e) {
                file.delete();
                LOGGER.error("KanBanNiang resources download failed. Reason: " + e.toString());
            }
        }
    }

    /**
     * Returns a random model (or selected).
     *
     * @param context the specified request context
     */
    public void randomModel(final RequestContext context) {
        final JsonRenderer renderer = new JsonRenderer();
        context.setRenderer(renderer);
        try {
            final String assets = "/plugins/kanbanniang/assets";
            String model;
            final ServletContext servletContext = SoloServletListener.getServletContext();
            try {
                model = optionRepository.get(Option.ID_C_KANBANNIANG_SELECTOR).optString(Option.OPTION_VALUE);
                if (model.isEmpty()) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                try (final InputStream inputStream = servletContext.getResourceAsStream(assets + "/model-list.json")) {
                    final JSONArray models = new JSONArray(IOUtils.toString(inputStream, "UTF-8"));
                    final int i = RandomUtils.nextInt(models.length());
                    model = models.getString(i);
                }
            }

            try (final InputStream modelResource = servletContext.getResourceAsStream(assets + "/model/" + model + "/index.json")) {
                final JSONObject index = new JSONObject(IOUtils.toString(modelResource, "UTF-8"));
                final JSONArray textures = index.optJSONArray("textures");
                if (textures.length() == 0) {
                    try (final InputStream texturesRes = servletContext.getResourceAsStream(assets + "/model/" + model + "/textures.json")) {
                        final JSONArray texturesArray = new JSONArray(IOUtils.toString(texturesRes, "UTF-8"));
                        final Object element = texturesArray.opt(RandomUtils.nextInt(texturesArray.length()));
                        if (element instanceof JSONArray) {
                            index.put("textures", element);
                        } else {
                            index.put("textures", new JSONArray().put(element));
                        }
                    }
                }
                renderer.setJSONObject(index);
            }
        } catch (final Exception e) {
            LOGGER.error("Returns a random KanBanNiang model failed.", e);
        }
    }

    /**
     * Returns a absolutely random model.
     *
     * @param context the specified request context
     */
    public void absolutelyRandomModel(final RequestContext context) {
        final JsonRenderer renderer = new JsonRenderer();
        context.setRenderer(renderer);
        try {
            final String assets = "/plugins/kanbanniang/assets";
            String model;
            final ServletContext servletContext = SoloServletListener.getServletContext();
            try (final InputStream inputStream = servletContext.getResourceAsStream(assets + "/model-list.json")) {
                final JSONArray models = new JSONArray(IOUtils.toString(inputStream, "UTF-8"));
                final int i = RandomUtils.nextInt(models.length());
                model = models.getString(i);
            }

            try (final InputStream modelResource = servletContext.getResourceAsStream(assets + "/model/" + model + "/index.json")) {
                final JSONObject index = new JSONObject(IOUtils.toString(modelResource, "UTF-8"));
                final JSONArray textures = index.optJSONArray("textures");
                if (textures.length() == 0) {
                    try (final InputStream texturesRes = servletContext.getResourceAsStream(assets + "/model/" + model + "/textures.json")) {
                        final JSONArray texturesArray = new JSONArray(IOUtils.toString(texturesRes, "UTF-8"));
                        final Object element = texturesArray.opt(RandomUtils.nextInt(texturesArray.length()));
                        if (element instanceof JSONArray) {
                            index.put("textures", element);
                        } else {
                            index.put("textures", new JSONArray().put(element));
                        }
                    }
                }
                renderer.setJSONObject(index);
            }
        } catch (final Exception e) {
            LOGGER.error("Returns a random KanBanNiang model failed.");
        }
    }

    /**
     * Get KanBanNiang skins loaded.
     *
     * @param context
     */
    public void kanbanniangList(final RequestContext context) {
        if (!Solos.isAdminLoggedIn(context)) {
            context.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        final String assets = "/plugins/kanbanniang/assets";
        final ServletContext servletContext = SoloServletListener.getServletContext();
        try (final InputStream inputStream = servletContext.getResourceAsStream(assets + "/model-list.json")) {
            final JSONArray models = new JSONArray(IOUtils.toString(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            Iterator iterator = models.iterator();
            if (models.length() != 0) {
                stringBuilder.append(iterator.next());
            }
            while (iterator.hasNext()) {
                stringBuilder.append(";" + iterator.next());
            }
            context.renderJSON().renderMsg(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
