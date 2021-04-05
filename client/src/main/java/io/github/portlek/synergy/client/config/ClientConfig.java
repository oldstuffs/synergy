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
import io.github.portlek.configs.annotation.Ignore;
import io.github.portlek.configs.configuration.FileConfiguration;
import io.github.portlek.configs.json.JsonType;
import io.github.portlek.synergy.core.util.SystemUtils;
import java.util.Locale;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents client's config.
 */
public final class ClientConfig implements ConfigHolder {

  /**
   * the configuration.
   */
  private static FileConfiguration configuration;

  /**
   * the client's language.
   */
  private static Locale lang = Locale.US;

  /**
   * the loader.
   */
  private static ConfigLoader loader;

  /**
   * lazy-init resource bundle.
   */
  @Nullable
  @Ignore
  private static ResourceBundle resource;

  /**
   * ctor.
   */
  private ClientConfig() {
  }

  /**
   * obtains the client language's resource bundle.
   *
   * @return resource bundle.
   */
  @NotNull
  public static ResourceBundle getLanguageBundle() {
    if (ClientConfig.resource == null) {
      ClientConfig.resource = ResourceBundle.getBundle("Synergy", ClientConfig.lang);
    }
    return ClientConfig.resource;
  }

  /**
   * loads the config.
   */
  public static void load() {
    ConfigLoader.builder()
      .setConfigHolder(new ClientConfig())
      .setConfigType(JsonType.get())
      .setFileName("client")
      .setFolder(SystemUtils.getHomePath())
      .build()
      .load(true);
  }

  /**
   * sets the client's language.
   *
   * @param lang the lang to set.
   */
  public static void setLanguage(@NotNull final Locale lang) {
    ClientConfig.lang = lang;
    ClientConfig.configuration.set("lang", lang.getLanguage() + "_" + lang.getCountry().toUpperCase(Locale.ROOT));
    ClientConfig.loader.save();
  }
}