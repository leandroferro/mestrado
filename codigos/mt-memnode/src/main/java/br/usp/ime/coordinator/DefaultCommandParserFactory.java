package br.usp.ime.coordinator;

import java.io.InputStream;

import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.DefaultCommandParser;

public class DefaultCommandParserFactory implements CommandParserFactory {

	@Override
	public CommandParser createFor(InputStream inputStream) {
		return new DefaultCommandParser(inputStream);
	}

}
