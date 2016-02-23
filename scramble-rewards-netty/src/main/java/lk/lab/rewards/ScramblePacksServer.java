package lk.lab.rewards;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class ScramblePacksServer {

	private final int port;
	
	public ScramblePacksServer(int port) {
		this.port = port;
	}
	
	public void run() throws Exception {
		final EventLoopGroup bossGroup = new NioEventLoopGroup();
		final EventLoopGroup workerGroup = new NioEventLoopGroup();
		final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(8);
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast("decoder", new HttpRequestDecoder());
						pipeline.addLast("encoder", new HttpResponseEncoder());
						pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
						pipeline.addLast(executorGroup, "scrambling", new ScramblePacksHandler());
					}
					
				});
			
			Channel ch = b.bind(port).sync().channel();
			System.out.println("Server started at port " + port + "...");
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			executorGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		
		new ScramblePacksServer(port).run();
	}
}
