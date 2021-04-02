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

import io.github.portlek.synergy.core.Server;
import io.github.portlek.synergy.core.Synergy;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.netty.Connections;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents coordinators.
 */
@Log4j2
public final class Coordinator extends Synergy {

  /**
   * the servers with id.
   */
  @NotNull
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
  public boolean onReceive(@NotNull final Protocol.AuthenticatedMessage packet, @NotNull final Channel channel) {
    return true;
  }

  @Override
  public void onVMShutdown() {
    Coordinator.log.info("VM shutting down, shutting down all servers (force)");
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
    CoordinatorConfig.load();
    this.channel = Connections.createConnection(new SynergyInitializer(this), CoordinatorConfig.ip, CoordinatorConfig.port)
      .await()
      .channel();
  }

  @Override
  protected void onTick() {
  }
}
