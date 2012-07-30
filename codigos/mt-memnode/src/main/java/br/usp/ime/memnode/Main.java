package br.usp.ime.memnode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	static final CountDownLatch endLatch = new CountDownLatch(2);
	static final CountDownLatch serverLatch = new CountDownLatch(1);
	
	static void logServer(String text) {
		System.err.flush();
		System.out.printf("[Server] %s%n", text);
		System.out.flush();
	}
	
	static void logClient(String text) {
		System.err.flush();
		System.out.printf("[Client] %s%n", text);
		System.out.flush();
	}
	
	static class Server implements Runnable {

		public void run() {
			try {
				ServerSocket server = new ServerSocket(6969);
				Main.serverLatch.countDown();
				
				logServer("Waiting for connection");
				Socket client = server.accept();
//				client.setOOBInline(true);
				logServer("Connection accepted: " + client);
				
				logServer("Reading from input");
				logServer("Read: " + (char)client.getInputStream().read());
				client.getOutputStream().write("TERMINOU!".getBytes());
				client.getOutputStream().flush();
				logServer("Finished OK");
				server.close();
			} catch (Exception e) {
				logServer("Finished with ERROR: " + e);
			}
			
			Main.endLatch.countDown();
		}
		
	}
	
	static class Client implements Runnable {

		public void run() {
			try {
				Socket client = new Socket();
				Main.serverLatch.await();
				
				logClient("Trying to connect");
				SocketAddress endpoint = new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 6969);
//				client.setTcpNoDelay(true);
				client.connect(endpoint);
				client.setSoTimeout(5000);
				logClient("Connected");
				
				logClient("Writing to output");
				
//				Random random = new Random(123);
//				for(String s : new String[]{"Leandro\n", "Ferro Luzia\n", "Denise\nCristina\tScalize"}) {
//					logClient("Writing \""+s+"\"");
//					client.getOutputStream().write(s.getBytes());
//					
//					if(random.nextBoolean()) {
//						client.getOutputStream().flush();
//						logClient("Flushing");
//					}
//				}
				
				logClient("Writing \" \"");
				client.getOutputStream().write(" ".getBytes());
				client.getOutputStream().flush();
				logClient("Flushed");
				
				logClient( "Lido do servidor " + client.getInputStream().read() );
				
				logClient("Finished OK");
				
				client.close();
			} catch (Exception e) {
				logClient("Finished with ERROR: " + e);
			} 
			
			Main.endLatch.countDown();
		}
		
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		ExecutorService pool = Executors.newFixedThreadPool(2);
		
		pool.execute(new Server());
		pool.execute(new Client());
		
		endLatch.await();
		
		pool.shutdown();
	}
}
