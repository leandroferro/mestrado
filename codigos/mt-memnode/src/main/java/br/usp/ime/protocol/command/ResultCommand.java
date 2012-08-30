package br.usp.ime.protocol.command;

import java.util.Arrays;

public class ResultCommand implements Command {

	private final byte[] id;
	private final byte[] data;

	public ResultCommand(byte[] id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + Arrays.hashCode(id);
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
		ResultCommand other = (ResultCommand) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (!Arrays.equals(id, other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResultCommand [id=" + Arrays.toString(id) + ", data="
				+ Arrays.toString(data) + "]";
	}

	public byte[] getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

	
}
