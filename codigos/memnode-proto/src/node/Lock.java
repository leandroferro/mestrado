package node;

public class Lock {

	public static enum Type {
		READ, WRITE
	};

	private final int address;
	private final Type type;

	public Lock(int address, Type type) {
		super();
		this.address = address;
		this.type = type;
	}

	public int getAddress() {
		return address;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Lock [address=" + address + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Lock other = (Lock) obj;
		if (address != other.address)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
