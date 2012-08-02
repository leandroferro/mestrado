package br.usp.ime.memnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Memnode {

	private ServerSocket serverSocket;
	private boolean stopped = false;

	public Memnode(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void start() {
		while(!stopped) {
			try {
				final Socket client;
				try {
					LoggingService.logOut("N", "Listening");
					client = serverSocket.accept();
					LoggingService.logOut("N", "Client: " + client);
				} catch (SocketTimeoutException e) {
					continue;
				}
				final InputStream inputStream = client.getInputStream();
				final OutputStream outputStream = client.getOutputStream();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void stop() {
		this.stopped = true;
	}

}
