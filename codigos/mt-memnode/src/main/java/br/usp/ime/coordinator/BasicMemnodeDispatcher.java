package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class BasicMemnodeDispatcher implements MemnodeDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(BasicMemnodeDispatcher.class);
			
	private final MemnodeMapper mapper;
	private final MemnodeClient client;

	public BasicMemnodeDispatcher(MemnodeMapper mapper, MemnodeClient client) {
		this.mapper = mapper;
		this.client = client;
	}

//	@Override
//	public Command dispatchAndCollect(Command command) {
//		if( command instanceof Minitransaction ) {
//			
//			Minitransaction minitransaction = (Minitransaction) command;
//
//			CommandBuilder builder = CommandBuilder.minitransaction(minitransaction.getId());
//			
//			Map<MemnodeReference, List<Command>> mapping = mapToReferences(minitransaction);
//			
//			logger.debug("Dispatching and collecting return with mapping {}", mapping);
//			
//			List<Command> collected = new ArrayList<Command>();
//			for( Entry<MemnodeReference, List<Command>> entry : mapping.entrySet() ) {
//				Command response = client.send(entry.getKey(), CommandBuilder.minitransaction(minitransaction.getId()).withCommands(entry.getValue()).build() );
//				
//				collected.add(response);
//			}
//			
//			return builder.withCommands(collected).build();
//		}
//		
//		return null;
//	}
//
//	@Override
//	public void dispatch(Command command) {
//		
//		if( command instanceof Minitransaction ) {
//			Minitransaction minitransaction = (Minitransaction) command;
//			
//			Map<MemnodeReference, List<Command>> mapping = mapToReferences(minitransaction);
//			
//			logger.debug("Dispatching and discarding return with mapping {}", mapping);
//			
//			for( Entry<MemnodeReference, List<Command>> entry : mapping.entrySet() ) {
//				client.send(entry.getKey(), CommandBuilder.minitransaction(minitransaction.getId()).withCommands(entry.getValue()).build() );
//			}
//		}
//		
//	}

//	private Map<MemnodeReference, List<Command>> mapToReferences(
//			Minitransaction minitransaction) {
//		Map<MemnodeReference, List<Command>> mapping = new HashMap<MemnodeReference, List<Command>>();
//		
//		logger.debug("Mapping {} to references", minitransaction);
//		
//		for( ReadCommand readCommand : minitransaction.getReadCommands() ) {
//			byte[] key = readCommand.getKey();
//			MemnodeReference reference = mapper.map(key);
//			
//			logger.debug("Mapped read of {} to {}", key, reference);
//			
//			if( !mapping.containsKey(reference) )
//				mapping.put(reference, new ArrayList<Command>());
//			
//			mapping.get(reference).add(readCommand);
//		}
//		
//		for( WriteCommand writeCommand : minitransaction.getWriteCommands() ) {
//			byte[] id = writeCommand.getId();
//			MemnodeReference reference = mapper.map(id);
//			
//			logger.debug("Mapped write of {} to {}", id, reference);
//			
//			if( !mapping.containsKey(reference) )
//				mapping.put(reference, new ArrayList<Command>());
//			
//			mapping.get(reference).add(writeCommand);
//		}
//		
//		for( ExtensionCommand extensionCommand : minitransaction.getExtensionCommands() ) {
//			byte[] key = extensionCommand.getParams().get(0).getValue();
//			MemnodeReference reference = mapper.map(key);
//			
//			logger.debug("Mapped extension with key {} to {}", key, reference);
//			
//			if( !mapping.containsKey(reference) )
//				mapping.put(reference, new ArrayList<Command>());
//			
//			mapping.get(reference).add(extensionCommand);
//		}
//		return mapping;
//	}

	@Override
	public String toString() {
		return "BasicMemnodeDispatcher [mapper=" + mapper + ", client="
				+ client + "]";
	}

	@Override
	public MemnodeMapping dispatchAndCollect(MemnodeMapping mapping) {

			MemnodeMapping collected = new MemnodeMapping(mapping.getMinitransactionId());
		
			logger.debug("Dispatching and collecting return with mapping {}",
					mapping);

			for (Entry<MemnodeReference, List<Command>> entry : mapping
					.entrySet()) {
				
				MemnodeReference reference = entry.getKey();
				Command response = client.send(reference,
						CommandBuilder.minitransaction(mapping.getMinitransactionId())
								.withCommands(entry.getValue()).build());

				if( response instanceof Minitransaction ) {
					Minitransaction minitransaction = (Minitransaction)response;
					
					collected.add(reference, minitransaction.getProblem());
					collected.add(reference, minitransaction.getNotCommitCommand());
					for( ResultCommand resultCommand : minitransaction.getResultCommands() ) {
						collected.add(reference, resultCommand);
					}
				}
				else {
					collected.add(reference, response);
				}
				
			}

			return collected;
	}

	@Override
	public void dispatch(MemnodeMapping mapping) {
		logger.debug("Dispatching without collecting return with mapping {}",
				mapping);

		for (Entry<MemnodeReference, List<Command>> entry : mapping
				.entrySet()) {
			
			MemnodeReference reference = entry.getKey();
			client.send(reference,
					CommandBuilder.minitransaction(mapping.getMinitransactionId())
							.withCommands(entry.getValue()).build());
		}
	}

	
}
