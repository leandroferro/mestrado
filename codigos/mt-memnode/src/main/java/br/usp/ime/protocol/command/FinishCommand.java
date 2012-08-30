package br.usp.ime.protocol.command;

public abstract class FinishCommand implements Command {

	private static FinishCommand _instance = new FinishCommand() {
	};
	
	private FinishCommand() {
		
	}
	
	public static FinishCommand instance() {
		return _instance;
	}
}
