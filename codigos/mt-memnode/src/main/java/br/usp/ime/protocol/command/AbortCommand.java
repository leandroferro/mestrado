package br.usp.ime.protocol.command;

public abstract class AbortCommand implements Command {

	private static AbortCommand _instance = new AbortCommand() {
	};
	
	private AbortCommand() {
		
	}
	
	public static AbortCommand instance() {
		return _instance;
	}
}
