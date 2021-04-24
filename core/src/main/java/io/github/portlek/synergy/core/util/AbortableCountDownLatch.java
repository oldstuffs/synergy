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

package io.github.portlek.synergy.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents abortable count down latches.
 */
public final class AbortableCountDownLatch extends CountDownLatch {

  /**
   * the aborted.
   */
  private boolean aborted = false;

  /**
   * ctor.
   *
   * @param count the count.
   */
  public AbortableCountDownLatch(final int count) {
    super(count);
  }

  /**
   * unblocks all threads waiting on this latch and cause them to receive an AbortedException.
   * if the latch has already counted all the way down, this method does nothing.
   */
  public void abort() {
    if (this.getCount() == 0) {
      return;
    }
    this.aborted = true;
    while (this.getCount() > 0) {
      this.countDown();
    }
  }

  @Override
  public void await() throws InterruptedException {
    super.await();
    if (this.aborted) {
      throw new AbortedException();
    }
  }

  @Override
  public boolean await(final long timeout, @NotNull final TimeUnit unit) throws InterruptedException {
    final var await = super.await(timeout, unit);
    if (this.aborted) {
      throw new AbortedException();
    }
    return await;
  }

  /**
   * an exception class that thrown when the latch aborted.
   */
  public static final class AbortedException extends InterruptedException {

    /**
     * ctor.
     */
    AbortedException() {
    }
  }
}
