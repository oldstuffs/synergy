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

package io.github.portlek.synergy.core.coordinator;

import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.KeyStore;
import io.github.portlek.synergy.api.Server;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents simple coordinators.
 */
@RequiredArgsConstructor
public final class SimpleCoordinator implements Coordinator {

  /**
   * the attributes.
   */
  @NotNull
  @Getter
  private final List<String> attributes;

  /**
   * the key store.
   */
  @NotNull
  @Delegate
  private final KeyStore keyStore;

  /**
   * the resources.
   */
  @NotNull
  @Getter
  private final Map<String, Integer> resources;

  /**
   * the servers.
   */
  @NotNull
  @Getter
  private final Map<String, Server> servers;

  /**
   * the channel.
   */
  @Nullable
  @Setter
  private Channel channel;

  /**
   * ctor.
   *
   * @param keyStore the key store.
   */
  public SimpleCoordinator(@NotNull final KeyStore keyStore) {
    this(new ObjectArrayList<>(), keyStore, new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
  }

  @NotNull
  @Override
  public Optional<Channel> getChannel() {
    return Optional.ofNullable(this.channel);
  }
}
