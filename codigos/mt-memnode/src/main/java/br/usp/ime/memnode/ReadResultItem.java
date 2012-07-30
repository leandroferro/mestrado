package br.usp.ime.memnode;

import java.util.Arrays;

public class ReadResultItem {

	private final byte[] key;
	private final byte[] value;

	public ReadResultItem(byte[] key, byte[] value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
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
		ReadResultItem other = (ReadResultItem) obj;
		if (!Arrays.equals(key, other.key))
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReadResultItem [key=" + Arrays.toString(key) + ", value="
				+ Arrays.toString(value) + "]";
	}
	
	

}
