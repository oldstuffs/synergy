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

package io.github.portlek.synergy.core.coordinator;

import io.github.portlek.configs.ConfigHolder;
import io.github.portlek.configs.ConfigLoader;
import io.github.portlek.configs.json.JsonType;
import io.github.portlek.synergy.core.util.SystemUtils;

/**
 * a class that represents config of the coordinators.
 */
public final class CoordinatorConfig implements ConfigHolder {

  /**
   * the ipd.
   */
  public static String ip = "0.0.0.0";

  /**
   * the port.
   */
  public static int port = 25501;

  /**
   * ctor.
   */
  private CoordinatorConfig() {
  }

  /**
   * loads the config.
   */
  public static void load() {
    ConfigLoader.builder()
      .setConfigHolder(new CoordinatorConfig())
      .setConfigType(JsonType.get())
      .setFileName("coordinator")
      .setFolder(SystemUtils.getHomePath())
      .build()
      .load(true);
  }
}
