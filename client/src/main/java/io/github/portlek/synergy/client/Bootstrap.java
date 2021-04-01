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

import io.github.portlek.synergy.client.commands.ClientCommands;
import io.github.portlek.synergy.client.util.SystemUtils;
import picocli.CommandLine;

/**
 * a main class of the client that Java runs first.
 */
public final class Bootstrap {

  /**
   * the synergy client version.
   */
  public static final String VERSION = "1.0.0-SNAPSHOT";

  /**
   * ctor.
   */
  private Bootstrap() {
  }

  /**
   * Java runs this method first when the client starts.
   *
   * @param args the args to start.
   */
  public static void main(final String[] args) {
    final var home = SystemUtils.getHome();
    final var code = new CommandLine(ClientCommands.class).execute(args);
    if (code != 0) {
      return;
    }
  }
}
