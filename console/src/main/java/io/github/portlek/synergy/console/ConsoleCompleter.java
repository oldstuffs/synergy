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
import io.github.portlek.synergy.api.CommandSender;
import io.github.portlek.synergy.languages.Languages;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/**
 * a class that represents console completer.
 */
@Log4j2
@RequiredArgsConstructor
public final class ConsoleCompleter implements Completer {

  /**
   * the command dispatcher.
   */
  @NotNull
  private final CommandDispatcher<CommandSender> commandDispatcher;

  /**
   * the console sender.
   */
  @NotNull
  private final CommandSender consoleSender;

  @Override
  public void complete(final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
    final var buffer = line.line();
    final var parse = this.commandDispatcher.parse(buffer, this.consoleSender);
    try {
      this.commandDispatcher.getCompletionSuggestions(parse).get().getList().stream()
        .filter(suggestion -> !suggestion.getText().isEmpty())
        .map(suggestion ->
          new Candidate(suggestion.getText(), suggestion.getText(), null, suggestion.getTooltip().getString(), null, null, true))
        .forEach(candidates::add);
    } catch (final ExecutionException e) {
      ConsoleCompleter.log.warn(Languages.getLanguageValue("unhandled-exception-tab-completing"), e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
