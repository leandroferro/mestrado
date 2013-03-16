package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import br.usp.ime.memnode.ByteArrayWrapper;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class SimpleMemnodeMapper implements MemnodeMapper {

	private final SortedSet<MemnodeReference> references = new TreeSet<MemnodeReference>(new Comparator<MemnodeReference>() {

		@Override
		public int compare(MemnodeReference o1, MemnodeReference o2) {
			byte[] address1 = o1.getAddress().getAddress().getAddress();
			byte[] address2 = o2.getAddress().getAddress().getAddress();
			
			for( int i = 0; i < Math.min(address1.length, address2.length); i++ ) {
				if( address1[i] < address2[i] ) {
					return -1;
				}
				else
					if( address1[i] > address2[i] ) {
						return 1;
					}
			}
			
			int port1 = o1.getAddress().getPort();
			int port2 = o2.getAddress().getPort();
			
			return port1 - port2;
		}
	});
	private final HashFunction hashFunction = Hashing.goodFastHash(32);
	
	private IdGenerator idGenerator;
	
	public SimpleMemnodeMapper(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
	
	public void add(MemnodeReference reference) {
		references.add(reference);
	}
	
	private MemnodeReference map(byte[] bytes) {
		int consistentHash = Hashing.consistentHash(hashFunction.hashBytes(bytes), references.size());
		return references.toArray(new MemnodeReference[0])[consistentHash];
	}

	@Override
	public String toString() {
		return "SimpleMemnodeMapper [references=" + references + "]";
	}

	@Override
	public MemnodeMapping map(Minitransaction minitransaction) {
		
		ByteArrayWrapper id = idGenerator.generate(new ByteArrayWrapper(minitransaction.getId()));
		
		MemnodeMapping mapping = new MemnodeMapping(id.value);
		
		for( ReadCommand readCommand : minitransaction.getReadCommands() ) {
			mapping.add(map(readCommand.getKey()), readCommand);
		}
		
		for( WriteCommand writeCommand : minitransaction.getWriteCommands() ) {
			mapping.add(map(writeCommand.getId()), writeCommand);
		}
		
		for( ExtensionCommand extensionCommand : minitransaction.getExtensionCommands() ) {
			mapping.add(map(extensionCommand.getParams().get(0).getValue()), extensionCommand);
		}
		
		return mapping;
	}

	
}
