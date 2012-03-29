package node;

public class ReadItem {

	private final int address;
	private final int length;

	public ReadItem(int address, int length) {
		this.address = address;
		this.length = length;
	}

	public int getAddress() {
		return address;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "ReadItem [address=" + address + ", length=" + length + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + length;
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
		ReadItem other = (ReadItem) obj;
		if (address != other.address)
			return false;
		if (length != other.length)
			return false;
		return true;
	}

	
}
