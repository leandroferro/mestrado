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
