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

import io.github.portlek.synergy.proto.Protocol;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an interface to determine transaction info.
 */
public interface TransactionInfo {

  /**
   * cancels the task.
   *
   * @return cancelled task.
   */
  @NotNull
  Optional<ScheduledFuture<Boolean>> getCancelTask();

  /**
   * sets the cancel task.
   *
   * @param future the future to set.
   */
  void setCancelTask(@NotNull ScheduledFuture<Boolean> future);

  /**
   * obtains the id.
   *
   * @return id.
   */
  @NotNull
  Optional<String> getIdOptional();

  /**
   * obtains the transaction listener.
   *
   * @return transaction listener.
   */
  @NotNull
  Optional<TransactionListener> getListener();

  /**
   * sets the listener.
   *
   * @param listener the listener to set.
   */
  void setListener(@NotNull TransactionListener listener);

  /**
   * obtains the target.
   *
   * @return target.
   */
  @NotNull
  Optional<String> getTarget();

  /**
   * sets the target.
   *
   * @param target the target to set.
   */
  void setTarget(@Nullable String target);

  /**
   * obtains the transaction.
   *
   * @return transaction.
   */
  @NotNull
  Optional<Protocol.Transaction> getTransaction();

  /**
   * sets the transaction.
   *
   * @param transaction transaction to set.
   */
  void setTransaction(@NotNull Protocol.Transaction transaction);

  /**
   * obtains the done.
   *
   * @return done
   */
  boolean isDone();

  /**
   * sets the done.
   *
   * @param done the done to set.
   */
  void setDone(boolean done);

  /**
   * sets the id.
   *
   * @param id the id to set.
   */
  void setId(@NotNull String id);
}
