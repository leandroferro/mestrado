package br.usp.ime.memnode;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDataStore implements DataStore {

	private static final Logger logger = LoggerFactory.getLogger(MapDataStore.class);
	private final Map<ByteArrayWrapper, ByteArrayWrapper> map;

	public MapDataStore(Map<ByteArrayWrapper, ByteArrayWrapper> map) {
		this.map = map;
	}

	public byte[] read(byte[] key) {
		ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
		ByteArrayWrapper wrapper = map.get(keyWrapper);
		byte[] returning;
		if (wrapper != null)
			returning = wrapper.value;
		else
			returning = null;
		
		logger.debug("Read of {} returned {}", key, wrapper);
		
		return returning;
	}

	public void write(byte[] key, byte[] data) {
		ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
		ByteArrayWrapper dataWrapper = new ByteArrayWrapper(data);
		map.put(keyWrapper, dataWrapper);
		logger.debug("Wrote ({},{}) to {}", new Object[]{keyWrapper, dataWrapper, map});
	}

	public void remove(byte[] key) {
		ByteArrayWrapper keyWrapper = new ByteArrayWrapper(key);
		map.remove(keyWrapper);
		logger.debug("Removed {} from {}", keyWrapper, map);
	}

	@Override
	public String toString() {
		return "MapDataStore [map=" + map + "]";
	}

	
}
