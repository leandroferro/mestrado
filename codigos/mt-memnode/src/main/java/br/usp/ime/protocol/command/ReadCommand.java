package br.usp.ime.protocol.command;

import java.util.Arrays;

public class ReadCommand implements Command {

	private final byte[] key;

	public ReadCommand(byte[] key) {
		this.key = key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
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
		ReadCommand other = (ReadCommand) obj;
		if (!Arrays.equals(key, other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReadCommand [key=" + Arrays.toString(key) + "]";
	}

	public byte[] getKey() {
		return key;
	}
	
}
