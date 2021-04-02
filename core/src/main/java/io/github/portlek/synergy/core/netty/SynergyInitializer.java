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

package io.github.portlek.synergy.core.netty;

import io.github.portlek.synergy.core.Synergy;
import io.github.portlek.synergy.proto.Protocol;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents a synergy's channel initializer.
 */
@RequiredArgsConstructor
public final class SynergyInitializer extends ChannelInitializer<NioSocketChannel> {

  /**
   * the synergy.
   */
  @NotNull
  private final Synergy synergy;

  @Override
  protected void initChannel(final NioSocketChannel ch) {
    ch.pipeline().addLast("lengthDecoder", new ProtobufVarint32FrameDecoder());
    ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(Protocol.AuthenticatedMessage.getDefaultInstance()));
    ch.pipeline().addLast("lengthPrepended", new ProtobufVarint32LengthFieldPrepender());
    ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
    ch.pipeline().addLast(new AuthenticatedMessageHandler(this.synergy));
  }
}
