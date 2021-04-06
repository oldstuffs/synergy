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

import io.github.portlek.synergy.core.Coordinator;
import io.github.portlek.synergy.core.Server;
import io.github.portlek.synergy.core.Synergy;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.netty.Connections;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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
 * a class that represents coordinators.
 */
@Log4j2
@RequiredArgsConstructor
public final class SynergyCoordinator extends Synergy implements Coordinator {

  /**
   * the address.
   */
  @NotNull
  @Getter
  private final InetSocketAddress address;

  /**
   * the id.
   */
  @NotNull
  @Getter
  private final String id;

  /**
   * the servers with id.
   */
  @NotNull
  @Getter
  private final Map<String, Server> servers = new ConcurrentHashMap<>();

  /**
   * the channel.
   */
  @Nullable
  private Channel channel;

  /**
   * obtains the channel.
   *
   * @return channel.
   */
  @NotNull
  public Channel getChannel() {
    return Objects.requireNonNull(this.channel, "not initiated");
  }

  @Override
  public void onClose() throws InterruptedException {
    this.running.set(false);
    this.getScheduler().shutdown();
    SynergyCoordinator.log.info("Connection closed!");
    SynergyCoordinator.log.info("Restarting in 5 seconds.");
    Thread.sleep(1000L * 5L);
    try {
      this.onStart();
    } catch (final InterruptedException ignored) {
    }
  }

  @Override
  public void onInit(@NotNull final NioSocketChannel channel) {
    // ignored.
  }

  @Override
  public boolean onReceive(@NotNull final Protocol.AuthenticatedMessage packet, @NotNull final Channel channel) {
    return true;
  }

  @Override
  public void onVMShutdown() {
    SynergyCoordinator.log.info("VM shutting down, shutting down all servers (force)");
    if (!this.getScheduler().isShutdown()) {
      this.getScheduler().shutdownNow();
    }
    this.servers.values().forEach(Server::close);
    if (this.channel != null && this.channel.isOpen()) {
      this.channel.close();
    }
  }

  @Override
  public void onStart() throws InterruptedException {
    SynergyCoordinator.log.info("Coordinator is starting.");
    final var ip = CoordinatorConfig.ip;
    final var port = CoordinatorConfig.port;
    SynergyCoordinator.log.info(String.format("Trying to connect network at %s:%s", ip, port));
    final var future = Connections.connect(new SynergyInitializer(this), ip, port)
      .await();
    if (!future.isSuccess()) {
      this.onClose();
      return;
    }
    this.channel = future.channel();
    this.channel.closeFuture()
      .addListener((ChannelFutureListener) ftr -> SynergyCoordinator.this.onClose());
    SynergyCoordinator.log.info("Connected.");
    this.running.set(true);
  }

  @Override
  protected void onTick() {
    this.sync();
  }

  /**
   * syncs with the network.
   */
  private void sync() {
    SynergyCoordinator.log.debug("Synced.");
  }
}
