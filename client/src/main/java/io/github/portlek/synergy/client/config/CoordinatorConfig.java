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
  public static InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 25501);

  /**
   * the attributes..
   */
  @NotNull
  public static List<String> attributes = List.of();

  /**
   * the coordinator id.
   */
  public static String id = UUID.randomUUID().toString();

  /**
   * the password.
   */
  public static String password = UUID.randomUUID().toString();

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
   * @param id the id to load.
   * @param password the password to load.
   * @param resources the resources to load.
   */
  public static void load(@Nullable final InetSocketAddress address, @Nullable final String[] attributes,
                          @Nullable final String id, @Nullable final String password,
                          @Nullable final Map<String, Integer> resources) {
    ConfigLoader.builder()
      .setConfigHolder(new CoordinatorConfig())
      .setConfigType(JsonType.get())
      .setFileName("coordinator")
      .setFolder(SystemUtils.getHomePath())
      .addLoaders()
      .build()
      .load(true);
    var saveNeeded = CoordinatorConfig.loadAddress(address);
    saveNeeded = saveNeeded || CoordinatorConfig.loadAttributes(attributes);
    saveNeeded = saveNeeded || CoordinatorConfig.loadId(id);
    saveNeeded = saveNeeded || CoordinatorConfig.loadPassword(password);
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
   * loads the id.
   *
   * @param id the id to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadId(@Nullable final String id) {
    final var finalId = Objects.isNull(id)
      ? CoordinatorConfig.id
      : id;
    if (!CoordinatorConfig.id.equals(finalId)) {
      CoordinatorConfig.id = finalId;
      CoordinatorConfig.section.set("id", finalId);
      return true;
    }
    return false;
  }

  /**
   * loads the password.
   *
   * @param password the password to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadPassword(@Nullable final String password) {
    final var finalPassword = Objects.isNull(password)
      ? CoordinatorConfig.password
      : password;
    if (!CoordinatorConfig.password.equals(finalPassword)) {
      CoordinatorConfig.password = finalPassword;
      CoordinatorConfig.section.set("password", finalPassword);
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
