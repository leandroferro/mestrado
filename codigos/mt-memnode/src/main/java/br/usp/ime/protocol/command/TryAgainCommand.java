package br.usp.ime.protocol.command;

public class TryAgainCommand implements Command {

	private static TryAgainCommand _instance = new TryAgainCommand() {
	};
	
	private TryAgainCommand() {
		
	}
	
	public static TryAgainCommand instance() {
		return _instance;
	}
	
}
