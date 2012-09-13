package br.usp.ime.coordinator;

import br.usp.ime.protocol.command.Command;

public interface MemnodeClient {

	Command send(MemnodeReference reference, Command command);

}
