package node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import node.Lock.Type;

public class LockManager {

	private final Set<Lock> locks = new HashSet<Lock>();
	
	private void lock(int start, int length, Type type) {
		for( int i = start; i < start+length; i++ )
			locks.add(new Lock(start, type));
	}
	
	private boolean isLocked(int start, int length, Type type) {
		for( int i = start; i < start+length; i++ )
			if( locks.contains(new Lock(start, type == Type.WRITE ? Type.READ : Type.WRITE)) )
				return true;
		
		return false;
	}
	
	public synchronized void unlock(int start, int length, Type type) {
		for( int i = start; i < start+length; i++ )
			locks.remove(new Lock(start, type));
	}

	public synchronized boolean tryLock(List<ReadItem> readsIterator) {
		for (ReadItem readItem : readsIterator) {
			if( isLocked(readItem.getAddress(), readItem.getLength(), Type.READ) )
				return false;
		}
		for (ReadItem readItem : readsIterator) {
			lock(readItem.getAddress(), readItem.getLength(), Type.READ);
		}
		return true;
	}
}
