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
import io.github.portlek.synergy.core.coordinator.SynergyCoordinator;
import io.github.portlek.synergy.core.network.SynergyNetwork;
import io.github.portlek.synergy.core.util.VMShutdownThread;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
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
public abstract class Synergy {

  /**
   * the running.
   */
  protected final AtomicBoolean running = new AtomicBoolean();

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
   * the shut down thread.
   */
  @Nullable
  private VMShutdownThread shutdownThread;

  /**
   * starts a {@link SynergyCoordinator} instance.
   *
   * @param address the address to start.
   * @param attributes the attributes to start.
   * @param id the id to start.
   * @param resources the resources to start.
   */
  public static void coordinator(@NotNull final InetSocketAddress address, @NotNull final List<String> attributes,
                                 @NotNull final String id, @NotNull final Map<String, Integer> resources) {
    new SynergyCoordinator(address, attributes, id, resources).start();
  }

  /**
   * starts a {@link SynergyNetwork} instance.
   *
   * @param address the address to start.
   * @param id the id to start.
   */
  public static void network(@NotNull final InetSocketAddress address, @NotNull final String id) {
    new SynergyNetwork(address, id).start();
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
   * runs when the synergy stops.
   */
  public abstract void onClose() throws InterruptedException;

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
   * starts the synergy.
   */
  protected final void start() {
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
    while (true) {
      try {
        Thread.sleep(5L);
      } catch (final InterruptedException e) {
        Synergy.log.fatal("Caught an exception at bootstrap level", e);
      }
    }
  }

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
}
