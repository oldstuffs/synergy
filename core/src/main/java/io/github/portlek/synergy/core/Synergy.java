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
import io.github.portlek.synergy.core.coordinator.Coordinator;
import io.github.portlek.synergy.core.coordinator.VMShutdownThread;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class that represents synergy types.
 */
@Log4j2
public abstract class Synergy {

  /**
   * the synergy.
   */
  @Nullable
  private static Synergy synergy;

  /**
   * async pool executor.
   */
  private final ThreadPoolExecutor asyncExecutor = new ThreadPoolExecutor(
    0, 4, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(),
    new ThreadFactoryBuilder()
      .setNameFormat("Synergy Async Task Handler Thread - %1$d")
      .build());

  /**
   * the scheduler.
   */
  @Getter
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  /**
   * starts and returns a {@link Coordinator} instance.
   */
  public static void coordinator() {
    final var synergy = Synergy.synergy = new Coordinator();
    Runtime.getRuntime().addShutdownHook(new VMShutdownThread(synergy));
    try {
      synergy.onStart();
    } catch (final InterruptedException e) {
      return;
    }
    synergy.scheduler.scheduleAtFixedRate(synergy::onTick, 0L, 50L, TimeUnit.MILLISECONDS);
    while (true) {
      try {
        Thread.sleep(5L);
      } catch (final InterruptedException e) {
        Synergy.log.fatal("Caught an exception at bootstrap level while running local coordinator", e);
      }
    }
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  public static Synergy getSynergy() {
    return Objects.requireNonNull(Synergy.synergy, "not initiated");
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
   * runs when receive a packet.
   *
   * @param packet the packet to receive.
   * @param channel the channel to receive.
   *
   * @return {@link false} if something goes wrong.
   */
  public abstract boolean onReceive(@NotNull Protocol.AuthenticatedMessage packet, @NotNull Channel channel);

  /**
   * runs when the V.M shut down.
   */
  public abstract void onVMShutdown();

  /**
   * runs when the synergy starts.
   */
  protected abstract void onStart() throws InterruptedException;

  /**
   * runs every 50ms.
   */
  protected abstract void onTick();
}
