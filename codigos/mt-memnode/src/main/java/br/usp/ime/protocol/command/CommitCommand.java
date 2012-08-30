package br.usp.ime.protocol.command;

public abstract class CommitCommand implements Command {

	private static CommitCommand _instance = new CommitCommand() {
	};
	
	private CommitCommand() {
		
	}
	
	public static CommitCommand instance() {
		return _instance;
	}
}
