package node;

import java.util.Arrays;

public class Controller {

	private byte[] buffer = new byte[1024];
	
	public void write(byte[] data, int position) {
		validatePosition(position);
		
		if( position + data.length <= buffer.length )
			System.arraycopy(data, 0, buffer, position, data.length );
		else
			System.arraycopy(data, 0, buffer, position, buffer.length - position);
	}

	private void validatePosition(int position) {
		if( position < 0 || position >= buffer.length )
			throw new InvalidPosition();
	}
	
	public byte[] read(int position, int length) {
		validatePosition(position);
		return Arrays.copyOfRange(buffer, position, Math.min(position + length, buffer.length));
	}
	
}
