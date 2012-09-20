package br.usp.ime.coordinator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketBasedConnection implements Connection{

	private final InetSocketAddress address;
	private Socket socket;

	public SocketBasedConnection(InetSocketAddress address) {
		super();
		this.address = address;
		try {
			this.socket = new Socket(address.getAddress(), address.getPort());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SocketBasedConnection other = (SocketBasedConnection) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Connection [address=" + address + "]";
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return socket.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getInputStream() {
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
}
