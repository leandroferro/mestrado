package br.usp.ime.protocol.command;

public abstract class NotCommitCommand implements Command{

	private static NotCommitCommand _instance = new NotCommitCommand() {
	};
	
	private NotCommitCommand() {
		
	}
	
	public static NotCommitCommand instance() {
		return _instance;
	}
}
