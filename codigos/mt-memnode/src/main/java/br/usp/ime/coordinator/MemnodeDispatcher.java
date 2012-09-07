package br.usp.ime.coordinator;

import br.usp.ime.protocol.command.Command;

public interface MemnodeDispatcher {

	Command dispatchAndCollect(Command command);

	void dispatch(Command command);

}
