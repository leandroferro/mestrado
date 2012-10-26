package br.usp.ime.memnode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class SimpleLockManager implements LockManager {

	private final static Logger logger = LoggerFactory
			.getLogger(SimpleLockManager.class);

	private final SetMultimap<ByteArrayWrapper, ByteArrayWrapper> readKeysMap = HashMultimap
			.create();
	private final SetMultimap<ByteArrayWrapper, ByteArrayWrapper> writeKeysMap = HashMultimap
			.create();
	private final SetMultimap<ByteArrayWrapper, ByteArrayWrapper> lockIdMap = HashMultimap
			.create();

	@Override
	public synchronized boolean acquire(ByteArrayWrapper lockId,
			List<ByteArrayWrapper> readKeys, List<ByteArrayWrapper> writeKeys) {

		logger.debug("Trying to acquire lock {} for {} and {}", new Object[] {
				lockId, readKeys, writeKeys });

		// checa se a chave de leitura ja possui uma trava de escrita
		for (ByteArrayWrapper readKey : readKeys) {
			if (writeKeysMap.containsKey(readKey)) {
				return false;
			}
		}

		// checa se a chave de escrita ja possui uma trava de escrita ou de
		// leitura
		for (ByteArrayWrapper writeKey : writeKeys) {
			if (readKeysMap.containsKey(writeKey)
					|| writeKeysMap.containsKey(writeKey)) {
				return false;
			}
		}

		// efetua as travas de leitura
		for (ByteArrayWrapper readKey : readKeys) {
			readKeysMap.put(readKey, lockId);
			lockIdMap.put(lockId, readKey);
		}

		// efetua as travas de escrita
		for (ByteArrayWrapper writeKey : writeKeys) {
			writeKeysMap.put(writeKey, lockId);
			lockIdMap.put(lockId, writeKey);
		}
		return true;
	}

	@Override
	public synchronized void release(ByteArrayWrapper lockId) {

		logger.debug("Releasing lock {}", lockId);

		Set<ByteArrayWrapper> lockIdKeys = lockIdMap.get(lockId);

		Iterator<ByteArrayWrapper> iterator = lockIdKeys.iterator();

		while (iterator.hasNext()) {
			ByteArrayWrapper key = iterator.next();
			iterator.remove();
			readKeysMap.remove(key, lockId);
			writeKeysMap.remove(key, lockId);
		}
	}

}
