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

package io.github.portlek.synergy.core.transaction;

import io.github.portlek.synergy.api.TransactionInfo;
import io.github.portlek.synergy.api.TransactionListener;
import io.github.portlek.synergy.proto.Protocol;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link TransactionInfo}
 */
@RequiredArgsConstructor
public final class SimpleTransactionInfo implements TransactionInfo {

  /**
   * the id.
   */
  @NotNull
  @Getter
  private final String id;

  /**
   * the cancel task.
   */
  @Nullable
  @Setter
  private ScheduledFuture<Boolean> cancelTask;

  /**
   * the done.
   */
  @Getter
  @Setter
  private boolean done;

  /**
   * the target.
   */
  @Nullable
  @Setter
  private String target;

  /**
   * the transaction.
   */
  @Nullable
  @Setter
  private Protocol.Transaction transaction;

  @NotNull
  @Override
  public Optional<ScheduledFuture<Boolean>> getCancelTask() {
    return Optional.ofNullable(this.cancelTask);
  }

  /**
   * the listener.
   */
  @Nullable
  @Setter
private TransactionListener listener;
  @NotNull
  @Override
  public Optional<TransactionListener> getListener() {
    return Optional.ofNullable(this.listener);
  }

  @NotNull
  @Override
  public Optional<String> getTarget() {
    return Optional.ofNullable(this.target);
  }

  @NotNull
  @Override
  public Optional<Protocol.Transaction> getTransaction() {
    return Optional.ofNullable(this.transaction);
  }
}
