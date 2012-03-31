package node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import node.ExecutionResult.Outcome;

import server.ReadResult;

public class Controller {

	private static final Logger logger = Logger.getLogger("Controller");
	private byte[] buffer = new byte[1024];
	private final LockManager lockManager = new LockManager();
	private final Map<String, List<WriteItem>> writeLog = new HashMap<String, List<WriteItem>>();
	
	void write(byte[] data, int position) {
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
	
	byte[] read(int position, int length) {
		validatePosition(position);
		return Arrays.copyOfRange(buffer, position, Math.min(position + length, buffer.length));
	}

	public ExecutionResult execute(Minitransaction minitransaction) {
		
		try {
			
			if( minitransaction.isAbort() ) {
				lockManager.unlock(minitransaction.getId());
				writeLog.remove(minitransaction.getId());
				return new ExecutionResult(Outcome.ABORT, minitransaction.getId(), Collections.<ReadResult>emptyList());
			}
			
			if( minitransaction.isCommit() ) {
				List<WriteItem> list = writeLog.get(minitransaction.getId());
				for (WriteItem writeItem : list) {
					write(writeItem.getData(), writeItem.getAddress());
				}
				lockManager.unlock(minitransaction.getId());
				writeLog.remove(minitransaction.getId());
				return new ExecutionResult(Outcome.COMMIT, minitransaction.getId(), Collections.<ReadResult>emptyList());
			}
			
			if( !lockManager.tryLock(minitransaction.getId(), minitransaction.getAllReads()) )
				return new ExecutionResult(Outcome.BAD_LOCK, minitransaction.getId(), Collections.<ReadResult>emptyList());
			
			if( !lockManager.tryLockToWrite(minitransaction.getId(),minitransaction.getWrites()) ) {
				lockManager.releaseLocks(minitransaction.getId(),minitransaction.getAllReads());
				return new ExecutionResult(Outcome.BAD_LOCK, minitransaction.getId(), Collections.<ReadResult>emptyList());
			}
			
			WaitItem waitItem = minitransaction.getWaitItem();
			
			if( waitItem != null ) {
				logger.info(minitransaction.getId() + " indo dormir por " + waitItem.getSeconds() + " segundos...");
				Thread.sleep(waitItem.getSeconds() * 1000);
				logger.info(minitransaction.getId() + " acordou!!!!");
			}
			
			Iterator<CompareItem> compareIterator = minitransaction.getCompareIterator();
			while (compareIterator.hasNext()) {
				CompareItem compareItem = compareIterator.next();
				
				byte[] data = read(compareItem.getAddress(), compareItem.getLength());
				
				if( !Arrays.equals(data, compareItem.getData()) ) {
					lockManager.releaseLocks(minitransaction.getId(),minitransaction.getAllReads());
					lockManager.releaseWriteLocks(minitransaction.getId(),minitransaction.getWrites());
					return new ExecutionResult(Outcome.ABORT, minitransaction.getId(), Collections.<ReadResult>emptyList());
				}
			}
			
			List<ReadResult> readResultList = new ArrayList<ReadResult>();
			Iterator<ReadItem> readIterator = minitransaction.getReadIterator();
			while (readIterator.hasNext()) {
				ReadItem readItem = readIterator.next();
				
				byte[] data = read(readItem.getAddress(), readItem.getLength());
				
				readResultList.add(new ReadResult(readItem.getAddress(), data));
			}

//			Iterator<WriteItem> writeIterator = minitransaction.getWriteIterator();
//			
//			while( writeIterator.hasNext() ) {
//				WriteItem writeItem = writeIterator.next();
//				write(writeItem.getData(), writeItem.getAddress());
//			}
			
			writeLog.put(minitransaction.getId(), minitransaction.getWrites());
			
//			lockManager.releaseLocks(minitransaction.getId(),minitransaction.getAllReads());
//			lockManager.releaseWriteLocks(minitransaction.getId(),minitransaction.getWrites());
			
			return new ExecutionResult(Outcome.COMMIT, minitransaction.getId(), readResultList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ExecutionResult(Outcome.ERROR, minitransaction.getId(), Collections.<ReadResult>emptyList());
		}
	}
	
}
