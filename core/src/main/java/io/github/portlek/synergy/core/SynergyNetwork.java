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

import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.Network;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.netty.Connections;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents networks.
 */
@Log4j2
@RequiredArgsConstructor
public final class SynergyNetwork extends Synergy implements Network {

  /**
   * the address.
   */
  @NotNull
  @Getter
  private final InetSocketAddress address;

  /**
   * the coordinators with id.
   */
  @NotNull
  @Getter
  private final Map<String, Coordinator> coordinators = new ConcurrentHashMap<>();

  /**
   * the id.
   */
  @NotNull
  @Getter
  private final String id;

  /**
   * the channel.
   */
  @Nullable
  private Channel channel;

  /**
   * starts a network instance.
   *
   * @param address the address to start.
   * @param id the id to start.
   */
  public static void start(@NotNull final InetSocketAddress address, @NotNull final String id) {
    (Synergy.instance = new SynergyNetwork(address, id))
      .start();
  }

  /**
   * obtains the channel.
   *
   * @return channel.
   */
  @NotNull
  public Channel getChannel() {
    return Objects.requireNonNull(this.channel, Languages.getLanguageValue("not-initiated"));
  }

  @Override
  public void onClose() throws InterruptedException {
    this.running.set(false);
    this.getScheduler().shutdown();
    SynergyNetwork.log.info(Languages.getLanguageValue("closed"));
    SynergyNetwork.log.info(Languages.getLanguageValue("restarting"));
    Thread.sleep(1000L * 5L);
    try {
      this.onStart();
    } catch (final InterruptedException ignored) {
    }
  }

  @Override
  public void onInit(@NotNull final NioSocketChannel channel) {
    final var address = channel.remoteAddress();
    SynergyNetwork.log.info(Languages.getLanguageValue("incoming-connection", address));
  }

  @Override
  public boolean onReceive(@NotNull final Protocol.AuthenticatedMessage packet, @NotNull final Channel channel) {
    return true;
  }

  @Override
  public void onVMShutdown() {
    SynergyNetwork.log.info(Languages.getLanguageValue("network-vm-shutting-down"));
    if (!this.getScheduler().isShutdown()) {
      this.getScheduler().shutdownNow();
    }
    this.coordinators.values().forEach(coordinator -> {
    });
    if (this.channel != null && this.channel.isOpen()) {
      this.channel.close();
    }
  }

  @Override
  public void onStart() throws InterruptedException {
    SynergyNetwork.log.info(Languages.getLanguageValue("network-is-starting"));
    SynergyNetwork.log.info(Languages.getLanguageValue("trying-to-bind", this.address));
    final var future = Connections.bind(new SynergyInitializer(this), this.address)
      .await();
    if (!future.isSuccess()) {
      this.onClose();
      return;
    }
    this.channel = future.channel();
    SynergyNetwork.log.info(Languages.getLanguageValue("bound"));
    this.running.set(true);
  }

  @Override
  protected void onTick() {
  }
}
