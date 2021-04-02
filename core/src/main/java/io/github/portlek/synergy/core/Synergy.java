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
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an abstract class that represents synergy types.
 */
public abstract class Synergy {

  /**
   * the instance.
   */
  @Nullable
  private static Synergy instance;

  /**
   * async pool executor.
   */
  @Getter
  public final ThreadPoolExecutor asyncExecutor = new ThreadPoolExecutor(
    0, 4, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(),
    new ThreadFactoryBuilder()
      .setNameFormat("Synergy Async Task Handler Thread - %1$d")
      .build());

  /**
   * starts and returns a {@link Coordinator} instance.
   */
  public static void coordinator() {
    final var synergy = Synergy.instance = new Coordinator();
    Runtime.getRuntime().addShutdownHook(new VMShutdownThread(synergy));
    synergy.onStart();
  }

  /**
   * obtains the instance.
   *
   * @return instance.
   */
  @NotNull
  public static Synergy getInstance() {
    return Objects.requireNonNull(Synergy.instance, "not initiated");
  }

  /**
   * runs when the synergy starts.
   */
  public abstract void onStart();

  /**
   * runs when the V.M shut down.
   */
  public abstract void onVMShutdown();
}
