package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class SimpleMemnodeMapper implements MemnodeMapper {

	private final List<MemnodeReference> references = new ArrayList<MemnodeReference>();
	
	public void add(MemnodeReference reference) {
		references.add(reference);
	}
	
	private MemnodeReference map(byte[] bytes) {
		return references.get( Arrays.hashCode(bytes) % references.size() );
	}

	@Override
	public String toString() {
		return "SimpleMemnodeMapper [references=" + references + "]";
	}

	@Override
	public MemnodeMapping map(Minitransaction minitransaction) {
		MemnodeMapping mapping = new MemnodeMapping(minitransaction.getId());
		
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
