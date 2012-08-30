package br.usp.ime.protocol.command;

import java.util.Arrays;

public class Param {

	private final byte[] value;

	public Param(byte[] value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
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
		Param other = (Param) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Param [value=" + Arrays.toString(value) + "]";
	}

	public byte[] getValue() {
		return value;
	};

}
