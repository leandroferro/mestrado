package br.usp.ime.coordinator;

import java.io.InputStream;

import br.usp.ime.protocol.parser.CommandParser;

public interface CommandParserFactory {

	CommandParser createFor(InputStream inputStream);

}
