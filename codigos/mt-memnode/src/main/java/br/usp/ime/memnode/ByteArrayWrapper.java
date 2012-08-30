package br.usp.ime.memnode;

import java.util.Arrays;

public class ByteArrayWrapper {

	public final byte[] value;

	public ByteArrayWrapper(byte[] value) {
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
		ByteArrayWrapper other = (ByteArrayWrapper) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ByteArrayWrapper [value=" + Arrays.toString(value) + "]";
	}

	
}
