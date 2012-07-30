package br.usp.ime.memnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Coordinator {

	public static final String INVALID_REQUEST_RESPONSE = errorResponse("Invalid input request received");
	
	public static final String INVALID_COMMAND_RESPONSE = errorResponse("Invalid command request received");

	public static final String INVALID_ID_SIZE_RESPONSE = errorResponse("Invalid id size received");

	private static String errorResponse(String message) {
		return "P " + message.length() + " " + message;
	}
	
	private final ServerSocket serverSocket;

	private boolean stopped = false;

	public Coordinator(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void start() {
		LoggingService.logOut("S", "Started");
		while(!stopped) {
			try {
				final Socket client;
				try {
					LoggingService.logOut("S", "Listening");
					client = serverSocket.accept();
					LoggingService.logOut("S", "Client: " + client);
				} catch (SocketTimeoutException e1) {
					continue;
				}
				final InputStream inputStream = client.getInputStream();
				final OutputStream outputStream = client.getOutputStream();
				
				int charRead = inputStream.read();
				
				if( charRead == -1 || (char)charRead != 'M' ) {
					LoggingService.logOut("S", "Read " + (char)charRead);
					writeSilently(outputStream, INVALID_COMMAND_RESPONSE.getBytes());
				}
				else {
					charRead = inputStream.read();
					if( charRead == -1 || (char)charRead != ' ' ){
						LoggingService.logOut("S", "Read " + (char)charRead);
						writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
					}
					else {
						final StringBuilder builder = new StringBuilder();
						while( (charRead = inputStream.read()) > -1 && (char)charRead != ' ' ) {
							builder.append((char)charRead);
						}
						try {
							final int size = Integer.parseInt(builder.toString());
							
							builder.setLength(0);
							
							if( charRead == -1 || (char)charRead != ' ' ){
								LoggingService.logOut("S", "Read " + (char)charRead);
								writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
							}
							else {
								for(int count = 0; count < size && (charRead = inputStream.read()) > -1; count++ ) {
									builder.append((char)charRead);
								}
								
								if( size != builder.length() ) {
									LoggingService.logOut("S", "Read " + (char)charRead);
									writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());	
								}
								
								charRead = inputStream.read();
								if( charRead == -1 || (char)charRead != ' ' ){
									LoggingService.logOut("S", "Read " + (char)charRead);
									writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());	
								}
								else {
									charRead = inputStream.read();
									if( charRead == -1 || (char)charRead != '{' ){
										LoggingService.logOut("S", "Read " + (char)charRead);
										writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
									}
									else {
										charRead = inputStream.read();
										if( charRead == -1 || (char)charRead != '\n' ){
											LoggingService.logOut("S", "Read " + (char)charRead);
											writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
										}
										else {
											charRead = inputStream.read();
											if( charRead == -1 || (char)charRead != '}' ){
												LoggingService.logOut("S", "Read " + (char)charRead);
												writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
											}
											else {
												charRead = inputStream.read();
												if( charRead == -1 || (char)charRead != '\n' ){
													LoggingService.logOut("S", "Read " + (char)charRead);
													writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
												}
												else {
													writeSilently(outputStream, ("M " + size + " " + builder.toString() + " {\n}").getBytes());
												}
											}
										}
									}
								}
							}
						} catch (NumberFormatException e) {
							writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());
						}
						
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	private static boolean writeSilently(OutputStream output, byte[] data) {
		try {
			output.write(data);
			output.write('\n');
			output.flush();
			return true;
		} catch (IOException e) {
			LoggingService.logOut("S", e);
			return false;
		}
	}

	public void stop() {
		this.stopped = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
