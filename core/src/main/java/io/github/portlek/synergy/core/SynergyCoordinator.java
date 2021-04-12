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

import com.google.protobuf.ByteString;
import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.CoordinatorServer;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.core.util.AuthUtils;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.netty.Connections;
import io.github.portlek.synergy.proto.Commands;
import io.github.portlek.synergy.proto.Core;
import io.github.portlek.synergy.proto.P3;
import io.github.portlek.synergy.proto.Protocol;
import io.github.portlek.synergy.proto.Protocols;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.List;
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
   * the attributes.
   */
  @NotNull
  @Getter
  private final List<String> attributes;

  /**
   * the id.
   */
  @NotNull
  @Getter
  private final String id;

  /**
   * the password.
   */
  @NotNull
  @Getter
  private final String password;

  /**
   * the provisioning servers.
   */
  private final Map<String, Core.Server> provisioningServers = new ConcurrentHashMap<>();

  /**
   * the resources.
   */
  @NotNull
  @Getter
  private final Map<String, Integer> resources;

  /**
   * the servers with id.
   */
  @NotNull
  @Getter
  private final Map<String, CoordinatorServer> servers = new ConcurrentHashMap<>();

  /**
   * the channel.
   */
  @Nullable
  private Channel channel;

  /**
   * starts a coordinator instance.
   *
   * @param address the address to start.
   * @param attributes the attributes to start.
   * @param id the id to start.
   * @param password the password to start.
   * @param resources the resources to start.
   */
  public static void start(@NotNull final InetSocketAddress address, @NotNull final List<String> attributes,
                           @NotNull final String id, @NotNull final String password,
                           @NotNull final Map<String, Integer> resources) {
    (Synergy.instance = new SynergyCoordinator(address, attributes, id, password, resources))
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
    SynergyCoordinator.log.info(Languages.getLanguageValue("connection-closed"));
    SynergyCoordinator.log.info(Languages.getLanguageValue("restarting"));
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
    SynergyCoordinator.log.info(Languages.getLanguageValue("coordinator-vm-shutting-down"));
    if (!this.getScheduler().isShutdown()) {
      this.getScheduler().shutdownNow();
    }
    this.servers.values().forEach(CoordinatorServer::close);
    if (this.channel != null && this.channel.isOpen()) {
      this.channel.close();
    }
  }

  @Override
  public boolean send(@NotNull final Protocol.Transaction message, @Nullable final String target) {
    if (this.channel == null || !this.channel.isActive()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-send-transaction", message.getId()));
      return false;
    }
    if (!message.isInitialized()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("transaction-not-initiated"));
      return false;
    }
    var messageBytes = message.toByteString();
    final var encBytes = AuthUtils.encrypt(messageBytes.toByteArray(), this.password);
    final var hash = AuthUtils.createHash(this.password, encBytes);
    messageBytes = ByteString.copyFrom(encBytes);
    final var auth = Protocol.AuthenticatedMessage.newBuilder()
      .setUuid(this.id)
      .setVersion(Protocols.PROTOCOL_VERSION)
      .setHash(hash)
      .setPayload(messageBytes)
      .build();
    if (!auth.isInitialized()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("message-not-initialized"));
      return false;
    }
    this.channel.writeAndFlush(auth);
    return true;
  }

  @Override
  public void onStart() throws InterruptedException {
    SynergyCoordinator.log.info(Languages.getLanguageValue("coordinator-is-starting"));
    SynergyCoordinator.log.info(Languages.getLanguageValue("trying-to-connect", this.address));
    final var future = Connections.connect(new SynergyInitializer(this), this.address)
      .await();
    if (!future.isSuccess()) {
      this.onClose();
      return;
    }
    this.channel = future.channel();
    this.channel.closeFuture()
      .addListener((ChannelFutureListener) ftr -> SynergyCoordinator.this.onClose());
    SynergyCoordinator.log.info(Languages.getLanguageValue("connected"));
    this.running.set(true);
  }

  @Override
  protected void onTick() {
    if (this.running.get()) {
      this.sync();
    }
  }

  /**
   * syncs with the network.
   *
   * @return {@code true} if sync is succeed.
   */
  private boolean sync() {
    final var syncBuilder = Commands.Sync.newBuilder()
      .setEnabled(this.running.get())
      .setName(this.id);
    this.resources.entrySet().stream()
      .map(entry ->
        Core.Resource.newBuilder()
          .setName(entry.getKey())
          .setValue(entry.getValue())
          .build())
      .forEach(syncBuilder::addResources);
    syncBuilder.addAllAttributes(this.attributes);
    this.servers.forEach((id, server) -> {
      final var meta = P3.P3Meta.newBuilder()
        .setId(server.getPackage().getId())
        .setVersion(server.getPackage().getVersion())
        .build();
      final var serverBuilder = Core.Server.newBuilder()
        .setP3(meta)
        .setUuid(server.getId())
        .setName(server.getName());
      server.getProperties().forEach((key, value) -> {
        final var prop = Core.Property.newBuilder()
          .setName(key)
          .setValue(value)
          .build();
        serverBuilder.addProperties(prop);
      });
      syncBuilder.addServers(serverBuilder.build());
    });
    this.provisioningServers.values().stream()
      .map(localServer -> Core.Server.newBuilder()
        .setUuid(localServer.getUuid())
        .setP3(localServer.getP3())
        .setActive(false)
        .addAllProperties(localServer.getPropertiesList())
        .setName(localServer.getName())
        .build())
      .forEach(syncBuilder::addServers);
    final var sync = syncBuilder.build();
    final var command = Commands.BaseCommand.newBuilder()
      .setType(Commands.BaseCommand.CommandType.SYNC)
      .setSync(sync)
      .build();
    final var info = this.transactionManager.generateInfo();
    final var message = this.transactionManager
      .build(info.getId(), Protocol.Transaction.Mode.SINGLE, command);
    if (message.isEmpty()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-build-message"));
      this.transactionManager.cancel(info.getId());
      return false;
    }
    SynergyCoordinator.log.info(Languages.getLanguageValue("sending-sync"));
    return this.transactionManager.send(info.getId(), message.get(), null);
  }
}
