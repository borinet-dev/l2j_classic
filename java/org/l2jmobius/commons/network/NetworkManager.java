/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.commons.network;

import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Nos
 */
public class NetworkManager
{
	private static final Logger LOGGER = Logger.getLogger(NetworkManager.class.getName());
	
	private final ServerBootstrap _serverBootstrap;
	private final String _host;
	private final int _port;
	
	private ChannelFuture _channelFuture;
	
	public NetworkManager(EventLoopGroup bossGroup, EventLoopGroup workerGroup, ChannelInitializer<SocketChannel> clientInitializer, String host, int port)
	{
		// @formatter:off
		_serverBootstrap = new ServerBootstrap()
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(clientInitializer)
			.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		// @formatter:on
		_host = host;
		_port = port;
	}
	
	public ChannelFuture getChannelFuture()
	{
		return _channelFuture;
	}
	
	public void start() throws InterruptedException
	{
		if ((_channelFuture != null) && !_channelFuture.isDone())
		{
			return;
		}
		
		_channelFuture = _serverBootstrap.bind(_host, _port).sync();
		LOGGER.info("게임서버: [" + _host + ":" + _port + "] 에 연결되었습니다.");
	}
	
	public void stop() throws InterruptedException
	{
		_channelFuture.channel().close().sync();
	}
}
