package br.usp.ime.protocol.parser;

import br.usp.ime.protocol.command.Command;

public interface CommandSerializer {

	String serialize(Command command);
	
}
