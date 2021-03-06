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

package io.github.portlek.synergy.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.CommandDispatcher;
import io.github.portlek.synergy.api.CommandSender;
import io.github.portlek.synergy.api.Synergy;
import io.github.portlek.synergy.api.TransactionInfo;
import io.github.portlek.synergy.api.TransactionManager;
import io.github.portlek.synergy.console.SynergyConsole;
import io.github.portlek.synergy.core.transaction.SimpleTransactionManager;
import io.github.portlek.synergy.core.util.VMShutdownThread;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.proto.Commands;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class that represents synergy types.
 */
@Log4j2
public abstract class BaseSynergy implements Synergy {

  /**
   * the running.
   */
  final AtomicBoolean running = new AtomicBoolean();

  /**
   * the transaction manager.
   */
  final TransactionManager transactionManager = new SimpleTransactionManager(this);

  /**
   * async pool executor.
   */
  private final ThreadPoolExecutor asyncExecutor = new ThreadPoolExecutor(
    0, 4, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(),
    new ThreadFactoryBuilder()
      .setNameFormat("Synergy Async Thread - %1$d")
      .build());

  /**
   * the console.
   */
  private final SynergyConsole console = new SynergyConsole(this);

  /**
   * the console thread.
   */
  private final Thread consoleThread = new Thread(this.console::start);

  /**
   * the scheduler.
   */
  @Getter
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
    new ThreadFactoryBuilder()
      .setNameFormat("Synergy Scheduler Thread - %1$d")
      .build());

  /**
   * the shut down thread.
   */
  @Nullable
  private VMShutdownThread shutdownThread;

  /**
   * generates a new id.
   *
   * @return a newly created id.
   */
  @NotNull
  public final String generateId() {
    return this.getId() + UUID.randomUUID();
  }

  @Override
  public final boolean isRunning() {
    return this.running.get();
  }

  @Override
  public final void shutdown() {
    BaseSynergy.log.info(Languages.getLanguageValue("shutting-down-synergy"));
    this.onVMShutdown();
  }

  /**
   * runs the given supplier as async.
   *
   * @param supplier the supplier to run.
   * @param <T> type of the value.
   *
   * @return completable future.
   */
  @NotNull
  public final <T> CompletableFuture<T> runAsync(@NotNull final Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, this.asyncExecutor);
  }

  /**
   * runs the given runnable as async.
   *
   * @param runnable the runnable to run.
   *
   * @return completable future.
   */
  @NotNull
  public final CompletableFuture<Void> runAsync(@NotNull final Runnable runnable) {
    return CompletableFuture.runAsync(runnable, this.asyncExecutor);
  }

  /**
   * runs when the synergy stops.
   */
  public abstract void onClose();

  /**
   * runs on initialization the channel.
   *
   * @param channel the channel to init.
   */
  public abstract void onInit(@NotNull NioSocketChannel channel);

  /**
   * runs when receive a packet.
   *
   * @param packet the packet to receive.
   * @param channel the channel to receive.
   *
   * @return {@code false} if something goes wrong.
   */
  public abstract boolean onReceive(@NotNull Protocol.AuthenticatedMessage packet, @NotNull Channel channel);

  /**
   * runs when the V.M shut down.
   */
  public abstract void onVMShutdown();

  /**
   * processes the given command.
   *
   * @param command the command to process.
   * @param info the info to process.
   * @param from the from to process.
   *
   * @return {@code true} if the command proceed successfully.
   */
  public abstract boolean process(@NotNull Commands.BaseCommand command, @NotNull TransactionInfo info,
                                  @Nullable String from);

  /**
   * sends the given message to the target.
   *
   * @param message the message to send.
   * @param target the target to set.
   *
   * @return {@code true} if the message was sent successfully..
   */
  public abstract boolean send(@NotNull Protocol.Transaction message, @Nullable String target);

  /**
   * runs when the synergy starts.
   *
   * @throws InterruptedException if the current thread was interrupted.
   */
  protected abstract void onStart() throws InterruptedException;

  /**
   * runs every 50ms.
   */
  protected abstract void onTick();

  /**
   * obtains the command dispatcher.
   *
   * @return command dispatcher.
   */
  @NotNull
  final CommandDispatcher<CommandSender> getCommandDispatcher() {
    return this.console.getCommandDispatcher();
  }

  /**
   * starts the synergy.
   */
  final void start() {
    final var runtime = Runtime.getRuntime();
    if (this.shutdownThread != null) {
      runtime.removeShutdownHook(this.shutdownThread);
      this.shutdownThread.interrupt();
    }
    runtime.addShutdownHook(this.shutdownThread = new VMShutdownThread(this));
    this.scheduler.scheduleAtFixedRate(this::onTick, 0L, 50L, TimeUnit.MILLISECONDS);
    try {
      this.onStart();
    } catch (final InterruptedException ignored) {
    }
    this.consoleThread.start();
    while (true) {
      try {
        Thread.sleep(5L);
      } catch (final InterruptedException e) {
        BaseSynergy.log.fatal(Languages.getLanguageValue("caught-an-exception"), e);
      }
    }
  }
}
