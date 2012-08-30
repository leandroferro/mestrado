package br.usp.ime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.CommandSerializer;

public class DummyClient {

	private static final Logger logger = LoggerFactory.getLogger(DummyClient.class);
			
	private final SocketAddress socketAddress;
	private Socket socket;
	
	public DummyClient(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	public void connect() {
		try {
			logger.trace("Connecting to {}", socketAddress);
			this.socket = new Socket();
			socket.setTcpNoDelay(true);
			socket.setSendBufferSize(1);
			socket.connect(socketAddress);
			logger.trace("Connection established {}", socket);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void send(Command command) {
		try {
			
			String serialize = CommandSerializer.serialize(command) + "\n";
			logger.trace("Sending {}", serialize);
			
			socket.getOutputStream().write(serialize.getBytes());
			socket.getOutputStream().flush();
			
			logger.trace("Sent {}", command);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Command receive() {
		try {
			InputStream inputStream = socket.getInputStream();
			
			CommandParser parser = new CommandParser(inputStream);
			
			Command command = parser.parseNext();
			
			logger.trace("Received {}", command);
			
			return command;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void disconnect() {
		try {
			logger.trace("Disconnecting from {}", socket);
			socket.close();
			logger.debug("Disconnected");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.socket = null;
		}
	}
}
