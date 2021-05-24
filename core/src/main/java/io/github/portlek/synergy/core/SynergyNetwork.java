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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.portlek.synergy.api.CommandSender;
import io.github.portlek.synergy.api.ConsoleInfo;
import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.KeyStore;
import io.github.portlek.synergy.api.Network;
import io.github.portlek.synergy.api.TransactionInfo;
import io.github.portlek.synergy.core.coordinator.SimpleCoordinator;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
import io.github.portlek.synergy.core.network.SimpleNetwork;
import io.github.portlek.synergy.core.util.AuthUtils;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.netty.Connections;
import io.github.portlek.synergy.proto.Commands;
import io.github.portlek.synergy.proto.Protocol;
import io.github.portlek.synergy.proto.Protocols;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represents networks.
 */
@Log4j2
@RequiredArgsConstructor
public final class SynergyNetwork extends BaseSynergy implements Network {

  /**
   * the address.
   */
  @NotNull
  @Getter
  private final InetSocketAddress address;

  /**
   * the consoles.
   */
  private final Map<String, ConsoleInfo> consoles = new ConcurrentHashMap<>();

  /**
   * the network.
   */
  @NotNull
  @Delegate
  private final Network network;

  /**
   * starts a network instance.
   *
   * @param address the address to start.
   * @param pool the pool to start.
   * @param id the id to start.
   * @param name the name to start.
   */
  public static void start(@NotNull final InetSocketAddress address, @NotNull final KeyStore.Pool pool,
                           @NotNull final String id, @NotNull final String name) {
    final var coordinatorMap = new ConcurrentHashMap<String, Coordinator>();
    pool.getKeyStores().stream()
      .map(SimpleCoordinator::new)
      .forEach(coordinator -> coordinatorMap.put(coordinator.getId(), coordinator));
    final var network = new SynergyNetwork(address, new SimpleNetwork(coordinatorMap, id, name));
    network.registerCommands();
    network.start();
  }

  /**
   * sends the given message to the channel.
   *
   * @param message the message to send.
   * @param channel the channel to set.
   * @param id the id to send.
   * @param key the key to send.
   *
   * @return {@code true} if the message was sent successfully.
   */
  private static boolean sendToChannel(@NotNull final Protocol.Transaction message, @NotNull final Channel channel,
                                       @NotNull final String id, @NotNull final String key) {
    if (!message.isInitialized()) {
      SynergyNetwork.log.error(Languages.getLanguageValue("transaction-not-initialized"));
      return false;
    }
    var messageBytes = message.toByteString();
    final var encBytes = AuthUtils.encrypt(messageBytes.toByteArray(), key);
    final var hash = AuthUtils.createHash(key, encBytes);
    messageBytes = ByteString.copyFrom(encBytes);
    final var auth = Protocol.AuthenticatedMessage.newBuilder()
      .setCoordinatorId(id)
      .setVersion(Protocols.PROTOCOL_VERSION)
      .setHash(hash)
      .setPayload(messageBytes)
      .build();
    if (!auth.isInitialized()) {
      SynergyNetwork.log.error(Languages.getLanguageValue("message-not-initialized"));
      return false;
    }
    channel.writeAndFlush(auth);
    return true;
  }

  @Override
  public void onClose() {
    this.running.set(false);
    SynergyNetwork.log.info(Languages.getLanguageValue("closed"));
    SynergyNetwork.log.debug(Languages.getLanguageValue("restarting"));
    try {
      Thread.sleep(1000L * 5L);
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
    final var id = packet.getCoordinatorId();
    final var sendInvalidMessage = new AtomicBoolean();
    final var coordinatorOptional = Optional.ofNullable(this.getCoordinators().get(id));
    final var packetPayload = packet.getPayload();
    if (coordinatorOptional.isEmpty()) {
      SynergyNetwork.log.error(Languages.getLanguageValue("unknown-coordinator-on-receive", id));
      sendInvalidMessage.set(true);
    } else if (!AuthUtils.validateHash(packet, coordinatorOptional.get().getPassword())) {
      SynergyNetwork.log.error(Languages.getLanguageValue("invalid-hash-on-message", id, packet.getHash(),
        AuthUtils.createHash(coordinatorOptional.get().getPassword(), packetPayload.toByteArray())));
      SynergyNetwork.log.error(Languages.getLanguageValue("closing-connection-bad-hash", channel));
      sendInvalidMessage.set(true);
    }
    if (coordinatorOptional.isEmpty() || sendInvalidMessage.get()) {
      SynergyNetwork.log.info(Languages.getLanguageValue("sending-invalid-message"));
      final var command = Commands.BaseCommand.newBuilder()
        .setType(Commands.BaseCommand.CommandType.NOOP)
        .build();
      final var message = Protocol.Transaction.newBuilder()
        .setId("0")
        .setMode(Protocol.Transaction.Mode.SINGLE)
        .setPayload(command)
        .build();
      SynergyNetwork.sendToChannel(message, channel, id, "0");
      channel.close();
      return false;
    }
    final var coordinator = coordinatorOptional.get();
    final var payload = ByteString.copyFrom(
      AuthUtils.decrypt(packetPayload.toByteArray(), coordinator.getPassword()));
    final Protocol.Transaction transaction;
    try {
      transaction = Protocol.Transaction.parseFrom(payload);
    } catch (final InvalidProtocolBufferException e) {
      SynergyNetwork.log.error(Languages.getLanguageValue("unable-to-read-transaction"), e);
      return false;
    }
    coordinator.getChannel()
      .filter(ch -> ch == channel)
      .ifPresent(ch ->
        channel.closeFuture().addListener(future -> {
          final var iterator = this.consoles.entrySet().iterator();
          while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var value = entry.getValue();
            final var target = value.getCoordinator();
            final var attached = value.getAttached();
            if (target.isPresent() && attached.isPresent() && Objects.equals(attached.get(), coordinator.getId())) {
              this.sendDetachConsole(target.get(), entry.getKey());
              iterator.remove();
            }
          }
        }));
    coordinator.setChannel(channel);
    this.transactionManager.receive(transaction, coordinator.getId());
    return true;
  }

  @Override
  public void onVMShutdown() {
    SynergyNetwork.log.info(Languages.getLanguageValue("network-vm-shutting-down"));
    if (!this.getScheduler().isShutdown()) {
      this.getScheduler().shutdownNow();
    }
    this.getCoordinators().values().forEach(coordinator -> {
    });
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
    return false;
  }

  @Override
  public boolean send(@NotNull final Protocol.Transaction message, @Nullable final String target) {
    return false;
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
    this.setChannel(future.channel());
    SynergyNetwork.log.info(Languages.getLanguageValue("bound"));
    this.running.set(true);
  }

  @Override
  protected void onTick() {
  }

  /**
   * registers the network commands.
   */
  private void registerCommands() {
    // Restart command.
    this.getCommandDispatcher().register(LiteralArgumentBuilder.<CommandSender>literal("restart")
      .executes(context -> {
        this.onClose();
        return Command.SINGLE_SUCCESS;
      }));
  }

  /**
   * sends detach console packet to the target.
   *
   * @param target the target to send.
   * @param consoleId the console id to send.
   *
   * @return {@code true} if the packet was sent successfully.
   */
  private boolean sendDetachConsole(@NotNull final String target, @NotNull final String consoleId) {
    final var coord = this.getCoordinators().get(target);
    if (coord == null) {
      SynergyNetwork.log.error(Languages.getLanguageValue("cannot-send-detach-console"), target);
      return false;
    }
    final var detach = Commands.DetachConsole.newBuilder()
      .setConsoleId(consoleId)
      .build();
    final var command = Commands.BaseCommand.newBuilder()
      .setType(Commands.BaseCommand.CommandType.DETACH_CONSOLE)
      .setDetachConsole(detach)
      .build();
    final var info = this.transactionManager.generateInfo();
    final var infoId = info.getIdOptional();
    if (infoId.isEmpty()) {
      return false;
    }
    final var id = infoId.get();
    final var message = this.transactionManager
      .build(id, Protocol.Transaction.Mode.SINGLE, command);
    if (message.isEmpty()) {
      SynergyNetwork.log.error(Languages.getLanguageValue("unable-to-build-transaction-detach-console"));
      this.transactionManager.cancel(id);
      return false;
    }
    SynergyNetwork.log.info(Languages.getLanguageValue("sending-detach-console"), consoleId);
    return this.transactionManager.send(id, message.get(), coord.getId());
  }
}
