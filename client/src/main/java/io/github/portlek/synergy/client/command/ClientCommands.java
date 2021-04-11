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
import io.github.portlek.synergy.client.config.CoordinatorConfig;
import io.github.portlek.synergy.client.config.NetworkConfig;
import io.github.portlek.synergy.core.Synergy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.annotations.Nullable;
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
  @CommandLine.Option(names = {"-d", "--debug"}, description = "Debug mode.")
  private boolean debug;

  /**
   * the client language.
   */
  @CommandLine.Option(names = {"-l", "--lang"}, description = "Client language.", converter = LocaleConverter.class)
  @Nullable
  private Locale lang;

  @Override
  public void run() {
    if (this.debug) {
      final var context = (LoggerContext) LogManager.getContext(false);
      context.getConfiguration()
        .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
        .setLevel(Level.DEBUG);
      context.updateLoggers();
    }
    if (this.lang != null) {
      ClientConfig.setLanguage(this.lang);
    }
    ClientConfig.loadLanguageBundle();
  }

  /**
   * runs the coordinator command.
   *
   * @param address the address to run.
   * @param attributes the attributes to run.
   * @param id the id to run.
   * @param resources the resources to run.
   */
  @CommandLine.Command(
    name = "coordinator"
  )
  void coordinator(
    @CommandLine.Option(names = "--address", description = "Coordinator address to connect.", converter = InetSocketAddressConverter.class) final InetSocketAddress address,
    @CommandLine.Option(names = "--attributes", description = "Coordinator attributes.") final String[] attributes,
    @CommandLine.Option(names = "--id", description = "Coordinator id.") final String id,
    @CommandLine.Option(names = "--resources", description = "Coordinator resources.") final Map<String, Integer> resources
  ) {
    this.run();
    CoordinatorConfig.load(address, attributes, id, resources);
    Synergy.coordinator(CoordinatorConfig.address, CoordinatorConfig.attributes, CoordinatorConfig.id,
      CoordinatorConfig.resources);
  }

  /**
   * runs the network command.
   *
   * @param address the address to run.
   * @param id the id to run.
   */
  @CommandLine.Command(
    name = "network"
  )
  void network(
    @CommandLine.Option(names = "--address", description = "Network address to bind.") final InetSocketAddress address,
    @CommandLine.Option(names = "--id", description = "Network id.") final String id
  ) {
    this.run();
    NetworkConfig.load(address, id);
    Synergy.network(NetworkConfig.address, NetworkConfig.id);
  }
}
