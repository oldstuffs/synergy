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

package io.github.portlek.synergy.client;

import org.jetbrains.annotations.NotNull;

/**
 * a main class of the client that Java runs first.
 */
public final class Main {

  /**
   * the home.
   */
  private static String home;

  /**
   * ctor.
   */
  private Main() {
  }

  /**
   * obtains the home.
   *
   * @return home.
   */
  @NotNull
  public static String getHome() {
    return Main.getHome(false);
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
      return Main.getHome0();
    }
    return Main.home == null
      ? Main.home = Main.getHome0()
      : Main.home;
  }

  /**
   * Java runs this method first when the client starts.
   *
   * @param args the args to start.
   */
  public static void main(final String[] args) {
    final var home = Main.getHome();
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