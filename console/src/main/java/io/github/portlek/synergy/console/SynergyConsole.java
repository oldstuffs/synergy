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

package io.github.portlek.synergy.console;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.portlek.synergy.api.CommandSender;
import io.github.portlek.synergy.api.Synergy;
import io.github.portlek.synergy.languages.Languages;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;

/**
 * a class that represents synergy's console.
 */
@Log4j2
@RequiredArgsConstructor
public final class SynergyConsole extends SimpleTerminalConsole {

  /**
   * the parser.
   */
  private static final DefaultParser PARSER = new DefaultParser();

  /**
   * the dispatcher.
   */
  @Getter
  private final CommandDispatcher<CommandSender> commandDispatcher = new CommandDispatcher<>();

  /**
   * the sender.
   */
  private final CommandSender sender = new ConsoleCommandSender();

  /**
   * the completer.
   */
  @NotNull
  private final ConsoleCompleter completer = new ConsoleCompleter(this.commandDispatcher, this.sender);

  /**
   * the synergy.
   */
  @NotNull
  private final Synergy synergy;

  @Override
  protected boolean isRunning() {
    return this.synergy.isRunning();
  }

  @Override
  protected void runCommand(final String command) {
    try {
      this.commandDispatcher.execute(command, this.sender);
    } catch (final CommandSyntaxException e) {
      if (e.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()) {
        this.sender.sendMessage(Languages.getLanguageValue("command-not-found", command));
        return;
      }
      SynergyConsole.log.error("An exception caught when running a command: ", e);
    }
  }

  @Override
  protected void shutdown() {
    this.synergy.shutdown();
  }

  @Override
  protected LineReader buildReader(final LineReaderBuilder builder) {
    return super.buildReader(builder
      .appName(this.synergy.getName())
      .completer(this.completer)
      .parser(SynergyConsole.PARSER)
      .variable(LineReader.HISTORY_FILE, Paths.get(".console_history")));
  }
}
