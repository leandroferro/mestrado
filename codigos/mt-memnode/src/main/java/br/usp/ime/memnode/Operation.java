package br.usp.ime.memnode;

import br.usp.ime.protocol.command.ExtensionCommand;

public interface Operation {

	ByteArrayWrapper identifier();

	boolean execute(DataStore dataStore, ExtensionCommand extensionCommand);

}
