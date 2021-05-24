/*
 * MIT License
 *
 * Copyright (c) 2021 Hasan Demirtaş
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.portlek.synergy.client.config;

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import io.github.portlek.configs.configuration.ConfigurationSection;
import io.github.portlek.configs.json.JsonType;
import io.github.portlek.synergy.api.KeyStore;
import io.github.portlek.synergy.core.util.SystemUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents config of the coordinators.
 */
public final class CoordinatorConfig implements ConfigHolder {

  /**
   * the address.
   */
  @NotNull
  public static InetSocketAddress address = new InetSocketAddress("localhost", 25501);

  /**
   * the attributes..
   */
  @NotNull
  public static List<String> attributes = List.of();

  /**
   * the key.
   */
  public static KeyStore key = new KeyStore.Impl(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
    UUID.randomUUID().toString());

  /**
   * the resources.
   */
  public static Map<String, Integer> resources = new Object2IntOpenHashMap<>();

  /**
   * the loader.
   */
  private static ConfigLoader loader;

  /**
   * the section.
   */
  private static ConfigurationSection section;

  /**
   * ctor.
   */
  private CoordinatorConfig() {
  }

  /**
   * loads the config.
   *
   * @param address the address to load.
   * @param attributes the attributes to load.
   * @param key the key to load.
   * @param resources the resources to load.
   */
  public static void load(@Nullable final InetSocketAddress address, @Nullable final String[] attributes,
                          @Nullable final KeyStore.Impl key, @Nullable final Map<String, Integer> resources) {
    ConfigLoader.builder()
      .setConfigHolder(new CoordinatorConfig())
      .setConfigType(JsonType.get())
      .setFileName("coordinator")
      .setFolder(SystemUtils.getHomePath())
      .addLoaders(KeyStore.Loader.INSTANCE)
      .build()
      .load(true);
    var saveNeeded = CoordinatorConfig.loadAddress(address);
    saveNeeded = saveNeeded || CoordinatorConfig.loadAttributes(attributes);
    saveNeeded = saveNeeded || CoordinatorConfig.loadKey(key);
    saveNeeded = saveNeeded || CoordinatorConfig.loadResources(resources);
    if (saveNeeded) {
      CoordinatorConfig.loader.save();
    }
  }

  /**
   * loads the address.
   *
   * @param address the address to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadAddress(@Nullable final InetSocketAddress address) {
    final var finalAddress = Objects.isNull(address)
      ? CoordinatorConfig.address
      : address;
    if (!CoordinatorConfig.address.equals(finalAddress)) {
      CoordinatorConfig.address = finalAddress;
      CoordinatorConfig.section.set("address", finalAddress);
      return true;
    }
    return false;
  }

  /**
   * loads the attributes.
   *
   * @param attributes the attributes to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadAttributes(@Nullable final String[] attributes) {
    final var finalAttributes = Objects.isNull(attributes)
      ? CoordinatorConfig.attributes
      : List.of(attributes);
    if (!CoordinatorConfig.attributes.equals(finalAttributes)) {
      CoordinatorConfig.attributes = finalAttributes;
      CoordinatorConfig.section.set("attributes", finalAttributes);
      return true;
    }
    return false;
  }

  /**
   * loads the key.
   *
   * @param key the key to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadKey(@Nullable final KeyStore key) {
    final var finalKey = Objects.isNull(key)
      ? CoordinatorConfig.key
      : key;
    if (!CoordinatorConfig.key.equals(finalKey)) {
      CoordinatorConfig.key = finalKey;
      CoordinatorConfig.section.set("password", finalKey);
      return true;
    }
    return false;
  }

  /**
   * loads the resources.
   *
   * @param resources the resources to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadResources(@Nullable final Map<String, Integer> resources) {
    final var finalResources = Objects.isNull(resources)
      ? CoordinatorConfig.resources
      : resources;
    if (CoordinatorConfig.resources != finalResources) {
      CoordinatorConfig.resources = finalResources;
      CoordinatorConfig.section.set("resources", finalResources);
      return true;
    }
    return false;
  }
}
