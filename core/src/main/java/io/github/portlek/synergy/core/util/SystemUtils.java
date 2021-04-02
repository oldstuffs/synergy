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

package io.github.portlek.synergy.core.util;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that contains utility methods for the system.
 */
public final class SystemUtils {

  /**
   * the home.
   */
  @Nullable
  private static String home;

  /**
   * the home path.
   */
  @Nullable
  private static Path homePath;

  /**
   * ctor.
   */
  private SystemUtils() {
  }

  /**
   * obtains the home.
   *
   * @return home.
   */
  @NotNull
  public static String getHome() {
    return SystemUtils.getHome(false);
  }

  /**
   * obtains the home.
   *
   * @param force the force to not use lazy initiated value.
   *
   * @return home.
   */
  @NotNull
  public static String getHome(final boolean force) {
    if (force) {
      return SystemUtils.getHome0();
    }
    return SystemUtils.home == null
      ? SystemUtils.home = SystemUtils.getHome0()
      : SystemUtils.home;
  }

  /**
   * obtains the home path.
   *
   * @return home path.
   */
  @NotNull
  public static Path getHomePath() {
    return SystemUtils.getHomePath(false);
  }

  /**
   * obtains the home path.
   *
   * @param force the force to not use lazy initiated value.
   *
   * @return home path.
   */
  @NotNull
  public static Path getHomePath(final boolean force) {
    if (force) {
      return Path.of(SystemUtils.getHome0());
    }
    return SystemUtils.homePath == null
      ? SystemUtils.homePath = Path.of(SystemUtils.getHome())
      : SystemUtils.homePath;
  }

  /**
   * obtains the home.
   *
   * @return home.
   */
  @NotNull
  private static String getHome0() {
    return System.getProperty("synergy_home",
      System.getProperty("SYNERGY_HOME",
        System.getProperty("user.dir")));
  }
}
