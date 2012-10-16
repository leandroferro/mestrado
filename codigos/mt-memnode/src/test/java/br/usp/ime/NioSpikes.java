package br.usp.ime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class NioSpikes {

	public static void main(String[] args) throws Exception {

		ExecutorService service = Executors.newCachedThreadPool();
		
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		System.out.printf("Channel created: %s%n", serverSocketChannel);
		
		ServerSocket serverSocket = serverSocketChannel.socket();
		System.out.printf("Socket created: %s%n", serverSocket);
		
		serverSocket.bind(new InetSocketAddress(10001));
		System.out.printf("Socket bound%n");
		
		Selector selector = Selector.open();
		SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.printf("Key registered for ACCEPT: %s%n", selectionKey);
		
		while( true ) {
			selector.select();
			
			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				
				System.out.printf("Key %s%n", key);
				
				SocketChannel socketChannel = ((ServerSocketChannel)key.channel()).accept();
				
				ByteBuffer bb = ByteBuffer.wrap("Ate mais".getBytes());
				socketChannel.write(bb);
				socketChannel.socket().close();
			}

			
		}
	}

}
