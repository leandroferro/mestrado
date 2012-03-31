package node;

public class Lock {

	public static enum Type {
		SHARED, EXCLUSIVE
	};

	private final String ownerTransactionId;
	private final int address;
	private final Type type;

	public Lock(String ownerTransactionId, int address, Type type) {
		this.ownerTransactionId = ownerTransactionId;
		this.address = address;
		this.type = type;
	}

	public String getOwnerTransactionId() {
		return ownerTransactionId;
	}

	public int getAddress() {
		return address;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Lock [ownerTransactionId=" + ownerTransactionId + ", address="
				+ address + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime
				* result
				+ ((ownerTransactionId == null) ? 0 : ownerTransactionId
						.hashCode());
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
		if (ownerTransactionId == null) {
			if (other.ownerTransactionId != null)
				return false;
		} else if (!ownerTransactionId.equals(other.ownerTransactionId))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
