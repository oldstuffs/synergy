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
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.KeyStore;
import io.github.portlek.synergy.api.Server;
import io.github.portlek.synergy.api.TransactionInfo;
import io.github.portlek.synergy.core.coordinator.SimpleCoordinator;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.core.util.AbortableCountDownLatch;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents coordinators.
 */
@Log4j2
@RequiredArgsConstructor
public final class SynergyCoordinator extends BaseSynergy implements Coordinator {

  /**
   * the address.
   */
  @NotNull
  @Getter
  private final InetSocketAddress address;

  /**
   * the coordinator.
   */
  @NotNull
  @Delegate
  private final Coordinator coordinator;

  /**
   * the close listener.
   */
  private final ChannelFutureListener closeListener = ftr -> SynergyCoordinator.this.onClose();

  /**
   * the latch.
   */
  private final AtomicReference<AbortableCountDownLatch> latch = new AtomicReference<>();

  /**
   * the provisioning servers.
   */
  private final Map<String, Core.Server> provisioningServers = new ConcurrentHashMap<>();

  /**
   * starts a coordinator instance.
   *
   * @param address the address to start.
   * @param attributes the attributes to start.
   * @param key the key to start.
   * @param resources the resources to start.
   */
  public static void start(@NotNull final InetSocketAddress address, @NotNull final List<String> attributes,
                           @NotNull final KeyStore key, @NotNull final Map<String, Integer> resources) {
    final var coordinator = new SimpleCoordinator(attributes, key, resources, new ConcurrentHashMap<>());
    new SynergyCoordinator(address, coordinator).start();
  }

  @Override
  public void onClose() {
    this.running.set(false);
    SynergyCoordinator.log.info(Languages.getLanguageValue("connection-closed"));
    SynergyCoordinator.log.debug(Languages.getLanguageValue("restarting"));
    try {
      Thread.sleep(1000L * 5L);
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
    if (!packet.getCoordinatorId().equals(this.getId()) || !AuthUtils.validateHash(packet, this.getPassword())) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("invalid-hash-on-message-coordinator"));
      System.err.println("Received an invalid hash on a message from the network coordinator.");
      System.err.println("This is likely due to us having an invalid UUID or secret key. Please check your coordinator.json!");
      channel.close();
      if (this.latch.get() != null) {
        this.latch.get().abort();
      }
      return false;
    }
    var payload = packet.getPayload();
    final var payloadBytes = AuthUtils.decrypt(payload.toByteArray(), this.getPassword());
    payload = ByteString.copyFrom(payloadBytes);
    try {
      final var transaction = Protocol.Transaction.parseFrom(payload);
      this.transactionManager.receive(transaction, null);
      return true;
    } catch (final InvalidProtocolBufferException e) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-read-transaction"), e);
      System.err.println("Received an unreadable message from the network coordinator.");
      System.err.println("This is likely due to us having an invalid UUID or secret key. Please check your coordinator.json!");
      channel.close();
      if (this.latch.get() != null) {
        this.latch.get().abort();
      }
      return false;
    }
  }

  @Override
  public void onVMShutdown() {
    SynergyCoordinator.log.info(Languages.getLanguageValue("coordinator-vm-shutting-down"));
    if (!this.getScheduler().isShutdown()) {
      this.getScheduler().shutdownNow();
    }
    this.getServers().values().forEach(Server::close);
    final var channelOptional = this.getChannel();
    if (channelOptional.isPresent()) {
      final var channel = channelOptional.get();
      if (channel.isOpen()) {
        channel.close();
      }
    }
  }

  @Override
  public boolean process(@NotNull final Commands.BaseCommand command, @NotNull final TransactionInfo info,
                         @Nullable final String from) {
    SynergyCoordinator.log.error(Languages.getLanguageValue("coordinator-cannot-process", command.getType()));
    return false;
  }

  @Override
  public boolean send(@NotNull final Protocol.Transaction message, @Nullable final String target) {
    if (this.getChannel().isEmpty() || !this.getChannel().get().isActive()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-send-transaction", message.getId()));
      return false;
    }
    if (!message.isInitialized()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("transaction-not-initialized"));
      return false;
    }
    final var encBytes = AuthUtils.encrypt(message.toByteString().toByteArray(), this.getPassword());
    final var hash = AuthUtils.createHash(this.getPassword(), encBytes);
    final var messageBytes = ByteString.copyFrom(encBytes);
    final var auth = Protocol.AuthenticatedMessage.newBuilder()
      .setCoordinatorId(this.getId())
      .setVersion(Protocols.PROTOCOL_VERSION)
      .setHash(hash)
      .setPayload(messageBytes)
      .build();
    if (!auth.isInitialized()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("message-not-initialized"));
      return false;
    }
    this.getChannel().get().writeAndFlush(auth);
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
    final var channel = future.channel();
    this.setChannel(channel);
    channel.closeFuture()
      .removeListener(this.closeListener)
      .addListener(this.closeListener);
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
   * sends coordinator create request to the network.
   *
   * @return {@code true} if the request was successfully sent.
   */
  private boolean sendCreateRequest() {
    final var command = Commands.BaseCommand.newBuilder()
      .setType(Commands.BaseCommand.CommandType.C_CREATE_COORDINATOR)
      .setCCreateCoordinator(Commands.C_CreateCoordinator.newBuilder()
        .setCoordinatorId(this.getId())
        .build())
      .build();
    final var mode = Protocol.Transaction.Mode.CREATE;
    final var optionalId = this.transactionManager.generateInfo().getIdOptional();
    if (optionalId.isEmpty()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("something-went-wrong"));
      return false;
    }
    final var transactionId = optionalId.get();
    final var built = this.transactionManager.build(
      transactionId,
      mode,
      command);
    if (built.isEmpty()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-build-message"));
      this.transactionManager.cancel(this.getId());
      return false;
    }
    SynergyCoordinator.log.debug(Languages.getLanguageValue("sending-coordinator-create-request"));
    return this.transactionManager.send(transactionId, built.get(), null);
  }

  /**
   * syncs with the network.
   *
   * @return {@code true} if sync is succeed.
   */
  private boolean sync() {
    final var syncBuilder = Commands.Sync.newBuilder()
      .setEnabled(this.running.get())
      .setCoordinatorId(this.getId());
    this.getResources().entrySet().stream()
      .map(entry ->
        Core.Resource.newBuilder()
          .setName(entry.getKey())
          .setValue(entry.getValue())
          .build())
      .forEach(syncBuilder::addResources);
    syncBuilder.addAllAttributes(this.getAttributes());
    this.getServers().forEach((id, server) -> {
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
    final var command = Commands.BaseCommand.newBuilder()
      .setType(Commands.BaseCommand.CommandType.SYNC)
      .setSync(syncBuilder.build())
      .build();
    final var mode = Protocol.Transaction.Mode.SINGLE;
    final var optionalId = this.transactionManager.generateInfo().getIdOptional();
    if (optionalId.isEmpty()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("something-went-wrong"));
      return false;
    }
    final var transactionId = optionalId.get();
    final var built = this.transactionManager.build(
      transactionId,
      mode,
      command);
    if (built.isEmpty()) {
      SynergyCoordinator.log.error(Languages.getLanguageValue("unable-to-build-message"));
      this.transactionManager.cancel(this.getId());
      return false;
    }
    SynergyCoordinator.log.debug(Languages.getLanguageValue("sending-sync"));
    return this.transactionManager.send(transactionId, built.get(), null);
  }
}
