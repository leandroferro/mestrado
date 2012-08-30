package br.usp.ime.memnode;

import java.util.HashMap;
import java.util.Map;

public class MapDataStore implements DataStore {

	private final Map<ByteArrayWrapper, ByteArrayWrapper> map = new HashMap<ByteArrayWrapper, ByteArrayWrapper>();
	
	public byte[] read(byte[] key) {
		ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
		ByteArrayWrapper wrapper = map.get(keyWrapper);
		if(wrapper != null)
			return wrapper.value;
		else
			return null;
	}

	public void write(byte[] key, byte[] data) {
		map.put(new ByteArrayWrapper(key), new ByteArrayWrapper(data));
	}

	public void remove(byte[] key) {
		map.remove(new ByteArrayWrapper(key));
	}

}
