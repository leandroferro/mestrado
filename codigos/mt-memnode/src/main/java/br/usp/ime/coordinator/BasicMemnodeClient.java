package br.usp.ime.coordinator;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.CommandSerializer;
import br.usp.ime.protocol.parser.DefaultCommandSerializer;

public class BasicMemnodeClient implements MemnodeClient {

	private static final Logger logger = LoggerFactory.getLogger(BasicMemnodeClient.class);
	
	private final CommandParserFactory parserFactory;
	private final ConnectionFactory connectionFactory;
	private final CommandSerializer commandSerializer;

	public BasicMemnodeClient(CommandParserFactory parserFactory, ConnectionFactory connectionFactory, CommandSerializer commandSerializer) {
		this.parserFactory = parserFactory;
		this.connectionFactory = connectionFactory;
		this.commandSerializer = commandSerializer;
	}

	public Command send(MemnodeReference reference, Command command) {
		
		Connection connection = connectionFactory.create(reference);

		logger.debug("Sending {} through {}", command, connection);

		OutputStream outputStream = connection.getOutputStream();
		
		try {
			outputStream.write( commandSerializer.serialize(command).getBytes() );
			outputStream.write("\n".getBytes());
			outputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException( e );
		}
		
		CommandParser parser = parserFactory.createFor( connection.getInputStream() );
		Command response = parser.parseNext();

		logger.debug("Memnode responded with {}", response);
		
		return response;
	}

	@Override
	public String toString() {
		return "BasicMemnodeClient [parserFactory=" + parserFactory
				+ ", connectionFactory=" + connectionFactory
				+ ", commandSerializer=" + commandSerializer + "]";
	}

	
}
