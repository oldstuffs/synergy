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
import io.github.portlek.synergy.client.command.converters.InetSocketAddressConverter;
import io.github.portlek.synergy.client.command.converters.LocaleConverter;
import io.github.portlek.synergy.client.config.ClientConfig;
import io.github.portlek.synergy.core.config.SynergyConfig;
import io.github.portlek.synergy.core.util.SystemUtils;
import io.github.portlek.synergy.languages.Languages;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import sun.misc.Unsafe;

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
   * the ascii art for Synergy Client text.
   */
  private static final String ART = "" +
    " ▄█▀▀▀█▄█                                                       \n" +
    "▄██    ▀█                                                       \n" +
    "▀███▄   ▀██▀   ▀██▀████████▄   ▄▄█▀██▀███▄███ ▄█▀███████▀   ▀██▀\n" +
    "  ▀█████▄ ██   ▄█   ██    ██  ▄█▀   ██ ██▀ ▀▀▄██  ██   ██   ▄█  \n" +
    "▄     ▀██  ██ ▄█    ██    ██  ██▀▀▀▀▀▀ ██    ▀█████▀    ██ ▄█   \n" +
    "██     ██   ███     ██    ██  ██▄    ▄ ██    ██          ███    \n" +
    "█▀█████▀    ▄█    ▄████  ████▄ ▀█████▀████▄   ███████    ▄█     \n" +
    "          ▄█                                 █▀     ██ ▄█       \n" +
    "        ██▀                                  ██████▀ ██▀        ";

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
    SynergyConfig.load();
    System.setProperty("io.netty.tryReflectionSetAccessible", "true");
    Bootstrap.disableWarning();
    final var exitCode = new CommandLine(ClientCommands.class)
      .registerConverter(InetSocketAddress.class, new InetSocketAddressConverter())
      .registerConverter(Locale.class, new LocaleConverter())
      .execute(args);
    if (exitCode == -1) {
      Languages.init(ClientConfig.getLanguageBundle());
    }
    final var listOfArgs = Arrays.stream(args)
      .map(s -> s.toLowerCase(Locale.ROOT))
      .collect(Collectors.toList());
    if (exitCode != -1) {
      if (exitCode == -2) {
        CommandLine.usage(ClientCommands.class, System.out);
      } else if (!listOfArgs.contains("coordinator") && !listOfArgs.contains("network")) {
        CommandLine.usage(ClientCommands.class, System.out);
      }
    }
    Bootstrap.log.info(Languages.getLanguageValue("closing-synergy"));
    System.exit(exitCode);
  }

  /**
   * disable some of warning with a tricky way.
   */
  private static void disableWarning() {
    try {
      final var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      final var u = (Unsafe) theUnsafe.get(null);
      final var cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
      final var logger = cls.getDeclaredField("logger");
      u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
    } catch (final Exception e) {
      // ignore
    }
  }
}
