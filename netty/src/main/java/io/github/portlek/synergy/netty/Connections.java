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

package io.github.portlek.synergy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains helpful method to create connections.
 */
@Log4j2
public final class Connections {

  /**
   * ctor.
   */
  private Connections() {
  }

  /**
   * binds to the given ip and port.
   *
   * @param initializer the initializer to bind.
   * @param ip the ip to bind.
   * @param port the port to bind.
   *
   * @return channel future.
   */
  @NotNull
  public static ChannelFuture bind(@NotNull final ChannelInitializer<NioSocketChannel> initializer,
                                   @NotNull final String ip, final int port) {
    return new ServerBootstrap()
      .group(new NioEventLoopGroup())
      .channel(NioServerSocketChannel.class)
      .childHandler(initializer)
      .option(ChannelOption.SO_BACKLOG, 128)
      .childOption(ChannelOption.SO_KEEPALIVE, true)
      .bind(ip, port);
  }

  /**
   * connects to the given ip and port.
   *
   * @param initializer the initializer to connect.
   * @param ip the ip to connect.
   * @param port the port to connect.
   *
   * @return channel future.
   */
  @NotNull
  public static ChannelFuture connect(@NotNull final ChannelInitializer<NioSocketChannel> initializer,
                                      @NotNull final String ip, final int port) {
    return new Bootstrap()
      .group(new NioEventLoopGroup())
      .channel(NioSocketChannel.class)
      .option(ChannelOption.SO_KEEPALIVE, true)
      .handler(initializer)
      .connect(ip, port);
  }
}
