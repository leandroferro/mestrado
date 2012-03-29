package node;

import java.util.Arrays;

public class WriteItem {

	private final byte[] data;
	private final int address;

	public WriteItem(int address, int length, byte[] data) {
		this.address = address;
		this.data = Arrays.copyOf(data, Math.min(length, data.length));
	}



	public byte[] getData() {
		return data;
	}



	public int getAddress() {
		return address;
	}



	@Override
	public String toString() {
		return "WriteItem [data=" + Arrays.toString(data) + ", address="
				+ address + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + Arrays.hashCode(data);
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
		WriteItem other = (WriteItem) obj;
		if (address != other.address)
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}
	
	

}
