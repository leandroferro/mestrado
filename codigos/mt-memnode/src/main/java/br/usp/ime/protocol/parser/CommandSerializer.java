package br.usp.ime.protocol.parser;

import br.usp.ime.memnode.ByteArrayWrapper;
import br.usp.ime.protocol.command.Command;

public interface CommandSerializer {

	ByteArrayWrapper serialize(Command command);
	
}
