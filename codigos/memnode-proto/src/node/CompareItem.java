package node;

import java.util.Arrays;

public class CompareItem extends ReadItem {

	private final byte[] data;

	public CompareItem(int address, int length, byte[] data) {
		super(address, length);
		this.data = data;
	}

	@Override
	public String toString() {
		return "CompareItem [address=" + getAddress() + ", length=" + getLength()
				+ ", data=" + Arrays.toString(data) + "]";
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompareItem other = (CompareItem) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

	
}
