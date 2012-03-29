package node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import node.ExecutionResult.Outcome;

import server.ReadResult;

public class Controller {

	private byte[] buffer = new byte[1024];
	private final LockManager lockManager = new LockManager();
	
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

	public ExecutionResult execute(Minitransaction minitransaction) {
		
		try {
			if( !lockManager.tryLock(minitransaction.getAllReads()) )
				return new ExecutionResult(Outcome.ABORT, minitransaction.getId(), Collections.<ReadResult>emptyList());
			
			Iterator<CompareItem> compareIterator = minitransaction.getCompareIterator();
			Iterator<ReadItem> readIterator = minitransaction.getReadIterator();

			
			
			Iterator<WriteItem> writeIterator = minitransaction.getWriteIterator();
			
			
			while( writeIterator.hasNext() ) {
				WriteItem writeItem = writeIterator.next();
				write(writeItem.getData(), writeItem.getAddress());
			}
			
			
			List<ReadResult> readResultList = new ArrayList<ReadResult>();
			
			while( readIterator.hasNext() ) {
				ReadItem readItem = readIterator.next();
				byte[] data = read( readItem.getAddress(), readItem.getLength() );
				readResultList.add(new ReadResult(readItem.getAddress(), data));
			}
			
			return new ExecutionResult(Outcome.COMMIT, minitransaction.getId(), readResultList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ExecutionResult(Outcome.ABORT, minitransaction.getId(), Collections.<ReadResult>emptyList());
		}
	}
	
}
