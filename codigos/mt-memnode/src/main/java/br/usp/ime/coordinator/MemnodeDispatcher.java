package br.usp.ime.coordinator;

import br.usp.ime.protocol.command.Command;

public interface MemnodeDispatcher {

//	public Command dispatchAndCollect(Command command);
	
//	public void dispatch(Command command);

	public MemnodeMapping dispatchAndCollect(MemnodeMapping mapping);

	public void dispatch(MemnodeMapping mapping);
	
}
