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

package io.github.portlek.synergy.languages;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an class that represents languages.
 */
@RequiredArgsConstructor
public final class Languages {

  /**
   * the instance.
   */
  @Nullable
  private static Languages instance;

  /**
   * the language cache.
   */
  @Getter
  private final Map<String, String> cache = new Object2ObjectOpenHashMap<>();

  /**
   * lazy-init resource bundle.
   */
  @NotNull
  @Getter
  private final ResourceBundle resource;

  /**
   * obtains the language value.
   *
   * @param key the key to get.
   * @param params the params to get.
   *
   * @return language value.
   */
  @NotNull
  public static String getLanguageValue(@NotNull final String key, @NotNull final Object... params) {
    final var languages = Languages.getInstance();
    final var value = languages.cache.computeIfAbsent(key, languages.resource::getString);
    if (params.length == 0) {
      return value;
    }
    return MessageFormat.format(value, params);
  }

  /**
   * initiates the languages.
   *
   * @param bundle the bundle to initiate.
   */
  public static void init(@NotNull final ResourceBundle bundle) {
    if (Languages.instance != null) {
      throw new IllegalStateException(Languages.getLanguageValue("cannot-initiate-twice"));
    }
    Languages.instance = new Languages(bundle);
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  private static Languages getInstance() {
    return Objects.requireNonNull(Languages.instance, "not initiated");
  }
}
