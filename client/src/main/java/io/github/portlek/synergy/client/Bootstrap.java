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

import io.github.portlek.synergy.client.command.ClientCommands;
import io.github.portlek.synergy.client.config.ClientConfig;
import io.github.portlek.synergy.core.util.SystemUtils;
import java.nio.file.Path;
import java.util.Locale;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

/**
 * a main class of the client that Java runs first.
 */
@Log4j2
public final class Bootstrap {

  /**
   * the home.
   */
  @NotNull
  public static final String HOME = SystemUtils.getHome();

  /**
   * the home path.
   */
  @NotNull
  public static final Path HOME_PATH = Path.of(Bootstrap.HOME);

  /**
   * the synergy client version.
   */
  public static final String VERSION = "1.0.0-SNAPSHOT";

  /**
   * the ascii art for Snergy Client text.
   */
  private static final String ART = "" +
    "(  ____ \\|\\     /|( (    /|(  ____ \\(  ____ )(  ____ \\|\\     /|\n" +
    "| (    \\/( \\   / )|  \\  ( || (    \\/| (    )|| (    \\/( \\   / )\n" +
    "| (_____  \\ (_) / |   \\ | || (__    | (____)|| |       \\ (_) / \n" +
    "(_____  )  \\   /  | (\\ \\) ||  __)   |     __)| | ____   \\   /  \n" +
    "      ) |   ) (   | | \\   || (      | (\\ (   | | \\_  )   ) (   \n" +
    "/\\____) |   | |   | )  \\  || (____/\\| ) \\ \\__| (___) |   | |   \n" +
    "\\_______)   \\_/   |/    )_)(_______/|/   \\__/(_______)   \\_/   \n" +
    "                                                               \n" +
    " _______  _       _________ _______  _       _________\n" +
    "(  ____ \\( \\      \\__   __/(  ____ \\( (    /|\\__   __/\n" +
    "| (    \\/| (         ) (   | (    \\/|  \\  ( |   ) (   \n" +
    "| |      | |         | |   | (__    |   \\ | |   | |   \n" +
    "| |      | |         | |   |  __)   | (\\ \\) |   | |   \n" +
    "| |      | |         | |   | (      | | \\   |   | |   \n" +
    "| (____/\\| (____/\\___) (___| (____/\\| )  \\  |   | |   \n" +
    "(_______/(_______/\\_______/(_______/|/    )_)   )_(   ";

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
    System.out.println(Bootstrap.ART);
    ClientConfig.load();
    new CommandLine(ClientCommands.class)
      .registerConverter(Locale.class, s -> {
        final var split = s.split("_");
        if (split.length != 2) {
          return null;
        }
        return new Locale(split[0], split[1].toUpperCase(Locale.ROOT));
      })
      .execute(args);
    while (true) {
      try {
        Thread.sleep(5L);
      } catch (final InterruptedException e) {
        Bootstrap.log.fatal("Caught an exception at bootstrap level", e);
      }
    }
  }
}
