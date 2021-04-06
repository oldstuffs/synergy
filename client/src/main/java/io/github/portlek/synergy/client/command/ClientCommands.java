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

package io.github.portlek.synergy.client.command;

import io.github.portlek.synergy.client.config.ClientConfig;
import io.github.portlek.synergy.core.Synergy;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import picocli.CommandLine;

/**
 * a class that that represents client's commands.
 */
@CommandLine.Command(
  name = "client",
  mixinStandardHelpOptions = true,
  exitCodeOnVersionHelp = -1,
  exitCodeOnUsageHelp = -1,
  versionProvider = ClientVersionProvider.class,
  showDefaultValues = true
)
public final class ClientCommands implements Runnable {

  /**
   * the debug mode.
   */
  @CommandLine.Option(names = {"-d", "--debug"},
    description = "Debug mode. Helpful for troubleshooting.")
  public static boolean debug = false;

  /**
   * the client language.
   */
  @CommandLine.Option(names = {"-l", "--lang"},
    description = "Client language. Change language of the client.")
  public static Locale lang = Locale.US;

  @Override
  public void run() {
    if (ClientCommands.debug) {
      final var context = (LoggerContext) LogManager.getContext(false);
      context.getConfiguration()
        .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        .setLevel(Level.DEBUG);
      context.updateLoggers();
    }
    ClientConfig.setLanguage(ClientCommands.lang);
    ClientConfig.getLanguageBundle();
  }

  /**
   * runs the coordinator command.
   *
   * @param id the id to run.
   * @param ip the ip to run.
   * @param port the port to run.
   */
  @CommandLine.Command(
    name = "coordinator"
  )
  void coordinator(@CommandLine.Option(names = "--id", defaultValue = "null") final String id,
                   @CommandLine.Option(names = "--ip", defaultValue = "null") final String ip,
                   @CommandLine.Option(names = "--port", defaultValue = "-1") final int port) {
    this.run();
    Synergy.coordinator(id, ip, port);
  }

  /**
   * runs the network command.
   *
   * @param id the id to run.
   * @param ip the ip to run.
   * @param port the port to run.
   */
  @CommandLine.Command(
    name = "network"
  )
  void network(@CommandLine.Option(names = "--id", defaultValue = "null") final String id,
               @CommandLine.Option(names = "--ip", defaultValue = "null") final String ip,
               @CommandLine.Option(names = "--port", defaultValue = "-1") final int port) {
    this.run();
    Synergy.network(id, ip, port);
  }
}
