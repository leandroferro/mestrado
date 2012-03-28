package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import node.Controller;

public class Server implements Runnable{

	private static final Logger logger = Logger.getLogger("Server");
	
	private final Controller controller;
	private final ServerSocket socket;
	private boolean shouldStop;

	public Server(Controller controller, ServerSocket socket) {
		super();
		this.controller = controller;
		this.socket = socket;
		
		shouldStop = false;
	}

	public void stop() {
		this.shouldStop = true;
	}
	
	@Override
	public void run() {
		logger.info("Esperando conexao");
		while( !shouldStop ) {
			logger.info("...");
			Socket client = null;
			try {
				client = socket.accept();
				
				logger.info("Aceitou conexao de " + client);
				
				InputStream inputStream = client.getInputStream();
				OutputStream outputStream = client.getOutputStream();
				
				int command = inputStream.read();
				logger.info("  Leu comando " + command);
				int position;
				int length;
				switch((char)command) {
				case 'e': // echo
					length = inputStream.read() - '0';
					for(int b = inputStream.read(); b != -1 && length-- > 0; b = inputStream.read()) {
						outputStream.write(b);
					}
					break;
				case 'w': // write
					position = inputStream.read() - '0'; 
					length = inputStream.read() - '0';
					ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
					for(int b = inputStream.read(); b != -1 && length-- > 0; b = inputStream.read())
						baos.write(b);
					byte[] data = baos.toByteArray();
					controller.write(data, position);
					outputStream.write(data);
					break;
				case 'r': // read
					position = inputStream.read() - '0';
					length = inputStream.read() - '0';
					outputStream.write(controller.read(position, length));
					break;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if( client != null )
					try {
						client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	}
	
}
