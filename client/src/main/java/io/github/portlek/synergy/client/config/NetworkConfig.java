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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents config of the networks.
 */
public final class NetworkConfig implements ConfigHolder {

  /**
   * the address.
   */
  public static InetSocketAddress address = new InetSocketAddress("localhost", 25501);

  /**
   * the coordinators.
   */
  public static List<? extends KeyStore> coordinators = new ObjectArrayList<>();

  /**
   * the coordinator id.
   */
  public static String id = UUID.randomUUID().toString();

  /**
   * the name.
   */
  public static String name = UUID.randomUUID().toString();

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
  private NetworkConfig() {
  }

  /**
   * loads the config.
   *
   * @param address the address to load.
   * @param coordinators the coordinator to load.
   * @param id the id to load.
   * @param name the name to load.
   */
  public static void load(@Nullable final InetSocketAddress address, @Nullable final List<? extends KeyStore> coordinators,
                          @Nullable final String id, @Nullable final String name) {
    ConfigLoader.builder()
      .setConfigHolder(new NetworkConfig())
      .setConfigType(JsonType.get())
      .setFileName("network")
      .setFolder(SystemUtils.getHomePath())
      .build()
      .load(true);
    var saveNeeded = NetworkConfig.loadAddress(address);
    saveNeeded = saveNeeded || NetworkConfig.loadCoordinators(coordinators);
    saveNeeded = saveNeeded || NetworkConfig.loadId(id);
    saveNeeded = saveNeeded || NetworkConfig.loadName(name);
    if (saveNeeded) {
      NetworkConfig.loader.save();
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
      ? NetworkConfig.address
      : address;
    if (!NetworkConfig.address.equals(finalAddress)) {
      NetworkConfig.address = finalAddress;
      NetworkConfig.section.set("address", finalAddress);
      return true;
    }
    return false;
  }

  /**
   * loads the coordinators.
   *
   * @param coordinators the coordinators to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadCoordinators(@Nullable final List<? extends KeyStore> coordinators) {
    final var finalCoordinators = Objects.isNull(coordinators)
      ? NetworkConfig.coordinators
      : coordinators;
    if (!NetworkConfig.coordinators.equals(finalCoordinators)) {
      NetworkConfig.coordinators = finalCoordinators;
      NetworkConfig.section.set("coordinators", finalCoordinators);
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
      ? NetworkConfig.id
      : id;
    if (!NetworkConfig.id.equals(finalId)) {
      NetworkConfig.id = finalId;
      NetworkConfig.section.set("id", finalId);
      return true;
    }
    return false;
  }

  /**
   * loads the name.
   *
   * @param name the name to load.
   *
   * @return {@code true} if the save is needed.
   */
  private static boolean loadName(@Nullable final String name) {
    final var finalName = Objects.isNull(name)
      ? NetworkConfig.name
      : name;
    if (!NetworkConfig.name.equals(finalName)) {
      NetworkConfig.name = finalName;
      NetworkConfig.section.set("name", finalName);
      return true;
    }
    return false;
  }
}
