package br.usp.ime.memnode;

import java.util.List;

public interface LockManager {

	boolean acquire(ByteArrayWrapper lockId, List<ByteArrayWrapper> readKeys, List<ByteArrayWrapper> writeKeys);

	void release(ByteArrayWrapper lockId);

}
