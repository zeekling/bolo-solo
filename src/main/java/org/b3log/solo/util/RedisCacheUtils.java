/*
 * Bolo - A stable and beautiful blogging system based in Solo.
 * Copyright (c) 2020, https://github.com/adlered
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
package org.b3log.solo.util;

import org.apache.commons.cli.CommandLine;
import org.b3log.latke.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

/**
 * @author zeekling
 */
public class RedisCacheUtils {

 private static final Logger LOGGER = Logger.getLogger(Markdowns.class);

 public static String REDIS_CACHE_ADDRESS = "127.0.0.1:6379";

 public static String REDIS_MODE = "single";

 public static boolean REDIS_ENABLE = false;

 public static boolean INITED = false;

 private static final Object LOCK = new Object();

 private static JedisPool jedisPool = null;

 public static void initRedisArgs(ResourceBundle solo) {
  if (INITED) {
   return;
  }
  boolean redisEnable = Boolean.parseBoolean(solo.getString("redisEnable"));
  RedisCacheUtils.REDIS_ENABLE = redisEnable;
  if (!redisEnable) {
   LOGGER.info("redis cache is not enable.");
   return;
  }
  LOGGER.info("redis cache enable.");
  String mode = solo.getString("redisMode");
  if ("single".equals(mode)) {
   RedisCacheUtils.REDIS_MODE = mode;
   RedisCacheUtils.REDIS_CACHE_ADDRESS = solo.getString("redisAddress");
  } else {
   LOGGER.error("Only support single mode.");
  }
  INITED = true;
 }

 public static void initRedisArgs(CommandLine commandLine) {
  if (!commandLine.hasOption("redisEnable")) {
   return;
  }
  if (INITED) {
   return;
  }
  LOGGER.info("command has redisEnable");
  boolean redisEnable = Boolean.parseBoolean(commandLine.getOptionValue("redisEnable"));
  RedisCacheUtils.REDIS_ENABLE = redisEnable;
  if (!redisEnable) {
   LOGGER.info("redis cache is not enable.");
   return;
  }
  LOGGER.info("redis cache enable.");
  String mode = commandLine.getOptionValue("redisMode");
  if ("single".equals(mode)) {
   RedisCacheUtils.REDIS_MODE = mode;
   RedisCacheUtils.REDIS_CACHE_ADDRESS = commandLine.getOptionValue("redisAddress");
  } else {
   LOGGER.error("Only support single mode.");
  }
  INITED = true;
 }

 public static void initJedis() {
  if (jedisPool != null) {
   return;
  }
  synchronized (LOCK) {
   if (jedisPool != null) {
    return;
   }
   String[] ipInfo = REDIS_CACHE_ADDRESS.split(":");
   if (ipInfo.length != 2) {
    LOGGER.warn("redisAddress is invalid");
    REDIS_ENABLE = false;
    return;
   }
   JedisPoolConfig config = new JedisPoolConfig();
   jedisPool = new JedisPool(config, ipInfo[0], Integer.parseInt(ipInfo[1]));
  }
 }

 public static String getCache(String cacheKey) {
  if (!REDIS_ENABLE) {
   return null;
  }
  try (Jedis jedis = jedisPool.getResource()) {
   return jedis.get(cacheKey);
  }
 }

 public static void cacheValue(String cacheKey, String cache) {
  if (!REDIS_ENABLE) {
   return ;
  }
  try (Jedis jedis = jedisPool.getResource()) {
   jedis.set(cacheKey, cache);
  }
 }

 public static void cleanCache(String... cacheKey) {
  if (!REDIS_ENABLE) {
   return ;
  }
  try (Jedis jedis = jedisPool.getResource()) {
   jedis.unlink(cacheKey);
  }
 }


}
