package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class BasicMemnodeDispatcher implements MemnodeDispatcher {

	private final MemnodeMapper mapper;
	private final MemnodeClient client;

	public BasicMemnodeDispatcher(MemnodeMapper mapper, MemnodeClient client) {
		this.mapper = mapper;
		this.client = client;
	}

	@Override
	public Command dispatchAndCollect(Command command) {
		if( command instanceof Minitransaction ) {
			
			Minitransaction minitransaction = (Minitransaction) command;

			CommandBuilder builder = CommandBuilder.minitransaction(minitransaction.getId());
			
			Map<MemnodeReference, List<Command>> mapping = new HashMap<MemnodeReference, List<Command>>();
			
			for( ReadCommand readCommand : minitransaction.getReadCommands() ) {
				MemnodeReference reference = mapper.map(readCommand.getKey());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(readCommand);
			}
			
			for( WriteCommand writeCommand : minitransaction.getWriteCommands() ) {
				MemnodeReference reference = mapper.map(writeCommand.getId());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(writeCommand);
			}
			
			for( ExtensionCommand extensionCommand : minitransaction.getExtensionCommands() ) {
				MemnodeReference reference = mapper.map(extensionCommand.getParams().get(0).getValue());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(extensionCommand);
			}
			
			List<Command> collected = new ArrayList<Command>();
			for( Entry<MemnodeReference, List<Command>> entry : mapping.entrySet() ) {
				Command response = client.send(entry.getKey(), CommandBuilder.minitransaction(minitransaction.getId()).withCommands(entry.getValue()).build() );
				
				collected.add(response);
			}
			
			return builder.withCommands(collected).build();
		}
		
		return null;
	}

	@Override
	public void dispatch(Command command) {
		
		if( command instanceof Minitransaction ) {
			Minitransaction minitransaction = (Minitransaction) command;
			
			Map<MemnodeReference, List<Command>> mapping = new HashMap<MemnodeReference, List<Command>>();
			
			for( ReadCommand readCommand : minitransaction.getReadCommands() ) {
				MemnodeReference reference = mapper.map(readCommand.getKey());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(readCommand);
			}
			
			for( WriteCommand writeCommand : minitransaction.getWriteCommands() ) {
				MemnodeReference reference = mapper.map(writeCommand.getId());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(writeCommand);
			}
			
			for( ExtensionCommand extensionCommand : minitransaction.getExtensionCommands() ) {
				MemnodeReference reference = mapper.map(extensionCommand.getParams().get(0).getValue());
				
				if( !mapping.containsKey(reference) )
					mapping.put(reference, new ArrayList<Command>());
				
				mapping.get(reference).add(extensionCommand);
			}
			
			for( Entry<MemnodeReference, List<Command>> entry : mapping.entrySet() ) {
				client.send(entry.getKey(), CommandBuilder.minitransaction(minitransaction.getId()).withCommands(entry.getValue()).build() );
			}
		}
		
	}

	@Override
	public String toString() {
		return "BasicMemnodeDispatcher [mapper=" + mapper + ", client="
				+ client + "]";
	}

	
}
