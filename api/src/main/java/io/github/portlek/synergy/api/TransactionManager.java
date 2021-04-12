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

package io.github.portlek.synergy.api;

import io.github.portlek.synergy.proto.Commands;
import io.github.portlek.synergy.proto.Protocol;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine transaction managers.
 */
public interface TransactionManager {

  /**
   * builds the transaction.
   *
   * @param id the id to build.
   * @param mode the mode to build.
   * @param command the command to build.
   *
   * @return built transaction.
   */
  @NotNull
  Optional<Protocol.Transaction> build(@NotNull String id, @NotNull Protocol.Transaction.Mode mode,
                                       @NotNull Commands.BaseCommand command);

  /**
   * cancels the transaction.
   *
   * @param id the id to cancel.
   *
   * @return {@code true} if the transaction was cancelled.
   */
  default boolean cancel(@NotNull final String id) {
    return this.cancel(id, false);
  }

  /**
   * cancels the transaction.
   *
   * @param id the id to cancel.
   * @param silentFail the silent fail.
   *
   * @return {@code true} if the transaction was cancelled.
   */
  boolean cancel(@NotNull String id, boolean silentFail);

  /**
   * completes the transaction.
   *
   * @param id the id to complete.
   *
   * @return {@code true} if the transaction was completed successfully.
   */
  boolean complete(@NotNull String id);

  /**
   * generate a transaction info instance.
   *
   * @return a newly created transaction info.
   */
  @NotNull
  TransactionInfo generateInfo();

  /**
   * obtains the transaction info.
   *
   * @param id the id to get.
   *
   * @return transaction info.
   */
  @NotNull
  Optional<TransactionInfo> getTransactionInfo(@NotNull String id);

  /**
   * receives the given message.
   *
   * @param message the message to receive.
   * @param from the from to receive.
   */
  void receive(@NotNull Protocol.Transaction message, @NotNull String from);

  /**
   * sends the transaction.
   *
   * @param id the id to send.
   * @param message the message to send.
   * @param target the target to send.
   *
   * @return {@code true} if the transaction was sent successfully.
   */
  boolean send(@NotNull String id, @NotNull Protocol.Transaction message, @Nullable String target);
}
