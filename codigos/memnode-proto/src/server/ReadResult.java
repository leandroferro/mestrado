package server;

public class ReadResult {

	private final int address;
	private final byte[] data;

	public ReadResult(int address, byte[] data) {
		this.address = address;
		this.data = data;
	}

	public int getAddress() {
		return address;
	}

	public byte[] getData() {
		return data;
	}

}
