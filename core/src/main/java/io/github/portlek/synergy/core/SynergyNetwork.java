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
import io.github.portlek.synergy.api.ConsoleInfo;
import io.github.portlek.synergy.api.Coordinator;
import io.github.portlek.synergy.api.Network;
import io.github.portlek.synergy.api.TransactionInfo;
import io.github.portlek.synergy.core.netty.SynergyInitializer;
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
   * the consoles.
   */
  private final Map<String, ConsoleInfo> consoles = new ConcurrentHashMap<>();

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
      .setId(id)
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
    final var id = packet.getId();
    final var sendInvalidMessage = new AtomicBoolean();
    final var coordinatorOptional = Optional.ofNullable(this.coordinators.get(id));
    if (coordinatorOptional.isEmpty()) {
      SynergyNetwork.log.error(Languages.getLanguageValue("unknown-coordinator-on-receive", packet.getId()));
      sendInvalidMessage.set(true);
    } else if (!AuthUtils.validateHash(packet, coordinatorOptional.get().getPassword())) {
      SynergyNetwork.log.error(Languages.getLanguageValue("invalid-hash-on-message", packet.getId(), packet.getHash(),
        AuthUtils.createHash(coordinatorOptional.get().getPassword(), packet.getPayload().toByteArray())));
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
      SynergyNetwork.sendToChannel(message, channel, packet.getId(), "0");
      channel.close();
      return false;
    }
    final var coordinator = coordinatorOptional.get();
    final var payload = ByteString.copyFrom(
      AuthUtils.decrypt(packet.getPayload().toByteArray(), coordinator.getPassword()));
    final Protocol.Transaction transaction;
    try {
      transaction = Protocol.Transaction.parseFrom(payload);
    } catch (final InvalidProtocolBufferException e) {
      SynergyNetwork.log.error(Languages.getLanguageValue("unable-to-read-transaction"), e);
      return false;
    }
    final var coordinatorChannel = coordinator.getChannel();
    if (coordinatorChannel.isPresent() && coordinatorChannel.get() == channel) {
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
      });
    }
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
    this.coordinators.values().forEach(coordinator -> {
    });
    if (this.channel != null && this.channel.isOpen()) {
      this.channel.close();
    }
  }

  @Override
  public boolean process(@NotNull final Commands.BaseCommand payload, @NotNull final TransactionInfo info,
                         @NotNull final String from) {
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
    this.channel = future.channel();
    SynergyNetwork.log.info(Languages.getLanguageValue("bound"));
    this.running.set(true);
  }

  @Override
  protected void onTick() {
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
    final var coord = this.coordinators.get(target);
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
    final var infoId = info.getId();
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
