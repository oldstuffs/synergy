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
import io.github.portlek.synergy.api.TransactionManager;
import io.github.portlek.synergy.core.Synergy;
import io.github.portlek.synergy.core.config.SynergyConfig;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.proto.Commands;
import io.github.portlek.synergy.proto.Protocol;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a simple implementation of {@link TransactionManager}.
 */
@Log4j2
public final class SimpleTransactionManager implements TransactionManager {

  /**
   * the transactions.
   */
  private final Map<String, TransactionInfo> transactions = new ConcurrentHashMap<>();

  @NotNull
  @Override
  public Optional<Protocol.Transaction> build(@NotNull final String id, @NotNull final Protocol.Transaction.Mode mode,
                                              @NotNull final Commands.BaseCommand command) {
    final var info = this.getTransactionInfo(id);
    if (info.isEmpty()) {
      SimpleTransactionManager.log.error(Languages.getLanguageValue("unable-to-build-transaction", id));
      return Optional.empty();
    }
    return Optional.of(Protocol.Transaction.newBuilder()
      .setId(info.get().getId())
      .setMode(mode)
      .setPayload(command)
      .build());
  }

  @Override
  public boolean cancel(@NotNull final String id, final boolean silentFail) {
    final var optional = this.getTransactionInfo(id);
    if (optional.isEmpty()) {
      if (!silentFail) {
        SimpleTransactionManager.log.error(Languages.getLanguageValue("cannot-cancel-transaction", id));
      }
      return false;
    }
    final var info = optional.get();
    info.getListener().ifPresent(listener -> listener.onCancel(this, info));
    final var cancelTask = info.getCancelTask();
    if (cancelTask.isPresent() && !cancelTask.get().isDone()) {
      cancelTask.get().cancel(false);
    }
    info.setDone(true);
    this.transactions.remove(id);
    return true;
  }

  @Override
  public boolean complete(@NotNull final String id) {
    final var optional = this.getTransactionInfo(id);
    if (optional.isEmpty()) {
      SimpleTransactionManager.log.error(Languages.getLanguageValue("cannot-complete-transaction", id));
      return false;
    }
    final var info = optional.get();
    info.getListener().ifPresent(listener -> listener.onComplete(this, info));
    final var cancelTask = info.getCancelTask();
    if (cancelTask.isPresent() && !cancelTask.get().isDone()) {
      cancelTask.get().cancel(false);
    }
    info.setDone(true);
    this.transactions.remove(id);
    return true;
  }

  @NotNull
  @Override
  public TransactionInfo generateInfo() {
    final var info = new SimpleTransactionInfo(
      Synergy.getInstance().generateId());
    this.transactions.put(info.getId(), info);
    final var tid = info.getId();
    info.setCancelTask(Synergy.getInstance().getScheduler().schedule(() -> {
      SimpleTransactionManager.log.warn(Languages.getLanguageValue("transaction-cancelled", tid));
      return this.cancel(tid, true);
    }, SynergyConfig.transactionTimeout, TimeUnit.SECONDS));
    return info;
  }

  @NotNull
  @Override
  public Optional<TransactionInfo> getTransactionInfo(@NotNull final String id) {
    return Optional.ofNullable(this.transactions.get(id));
  }

  @Override
  public boolean send(@NotNull final String id, @NotNull final Protocol.Transaction message,
                      @Nullable final String target) {
    final var optional = this.getTransactionInfo(id);
    if (optional.isEmpty()) {
      SimpleTransactionManager.log.error(Languages.getLanguageValue("cannot-send-transaction", id));
      return false;
    }
    final var info = optional.get();
    if (!info.getId().equals(message.getId())) {
      SimpleTransactionManager.log.error(Languages.getLanguageValue("message-id-does-not-match", id));
      return false;
    }
    info.setTransaction(message);
    info.setTarget(target);
    info.getListener().ifPresent(listener -> listener.onSend(this, info));
    if (message.getMode() == Protocol.Transaction.Mode.COMPLETE || message.getMode() == Protocol.Transaction.Mode.SINGLE) {
      this.complete(info.getId());
    }
    return Synergy.getInstance().send(message, info.getTarget().orElse(null));
  }
}
