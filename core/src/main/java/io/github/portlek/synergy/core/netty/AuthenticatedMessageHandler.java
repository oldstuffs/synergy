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

import io.github.portlek.synergy.core.BaseSynergy;
import io.github.portlek.synergy.languages.Languages;
import io.github.portlek.synergy.proto.Protocol;
import io.github.portlek.synergy.proto.Protocols;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represents authenticated message handlers.
 */
@Log4j2
@RequiredArgsConstructor
public final class AuthenticatedMessageHandler extends SimpleChannelInboundHandler<Protocol.AuthenticatedMessage> {

  /**
   * synergy.
   */
  @NotNull
  private final BaseSynergy synergy;

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    ctx.channel().close();
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final Protocol.AuthenticatedMessage msg) {
    if (msg.getVersion() != Protocols.PROTOCOL_VERSION) {
      AuthenticatedMessageHandler.log.error(Languages.getLanguageValue("protocol-version-mismatch",
        Protocols.PROTOCOL_VERSION, msg.getVersion()));
      AuthenticatedMessageHandler.log.error(Languages.getLanguageValue("disconnecting-version-mismatch"));
      ctx.channel().close();
      return;
    }
    if (!this.synergy.onReceive(msg, ctx.channel())) {
      AuthenticatedMessageHandler.log.error(Languages.getLanguageValue("message-failed"));
    }
  }
}
