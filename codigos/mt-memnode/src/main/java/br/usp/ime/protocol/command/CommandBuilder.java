package br.usp.ime.protocol.command;

import java.util.ArrayList;
import java.util.List;



public abstract class CommandBuilder {

	private static class ProblemBuilder extends CommandBuilder {

		private final byte[] description;

		public ProblemBuilder(byte[] description) {
			this.description = description;
		}

		@Override
		public Command build() {
			return new Problem(description);
		}

	}

	private CommandBuilder() {

	}

	private static class MinitransactionBuilder extends CommandBuilder {

		private final byte[] id;
		private Problem problem;
		private final List<ReadCommand> readCommands = new ArrayList<ReadCommand>();
		private final List<WriteCommand> writeCommands = new ArrayList<WriteCommand>();
		private final List<ExtensionCommand> extensionCommands = new ArrayList<ExtensionCommand>();
		private CommitCommand commitCommand;
		private final List<ResultCommand> resultCommands = new ArrayList<ResultCommand>();
		private FinishCommand finishCommand;

		public MinitransactionBuilder(byte[] id) {
			this.id = id;
		}

		@Override
		public CommandBuilder withProblem(Problem problem) {
			this.problem = problem;
			return this;
		}

		@Override
		public CommandBuilder withReadCommand(ReadCommand readCommand) {
			readCommands.add(readCommand);
			return this;
		}

		@Override
		public CommandBuilder withWriteCommand(WriteCommand writeCommand) {
			writeCommands.add(writeCommand);
			return this;
		}

		@Override
		public CommandBuilder withExtensionCommand(
				ExtensionCommand extensionCommand) {
			extensionCommands.add(extensionCommand);
			return this;
		}
		
		@Override
		public CommandBuilder withCommitCommand() {
			this.commitCommand = CommitCommand.instance();
			return this;
		}

		@Override
		public CommandBuilder withResultCommand(ResultCommand resultCommand) {
			resultCommands.add( resultCommand );
			return this;
		}

		@Override
		public CommandBuilder withFinishCommand() {
			finishCommand = FinishCommand.instance();
			return this;
		}

		@Override
		public Command build() {
			return new Minitransaction(id, problem, readCommands, writeCommands, extensionCommands, commitCommand, resultCommands, finishCommand);
		}

	}

	public static CommandBuilder minitransaction(byte[] id)
			throws IllegalArgumentException {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		return new MinitransactionBuilder(id);
	}

	public CommandBuilder withProblem(Problem problem) {
		throw new IllegalStateException();
	}

	public CommandBuilder withWriteCommand(WriteCommand writeCommand) {
		throw new IllegalStateException();
	}
	
	public CommandBuilder withReadCommand(ReadCommand readCommand) {
		throw new IllegalStateException();
	}

	public Command build() {
		throw new IllegalStateException();
	}

	public static CommandBuilder problem(byte[] description) {
		return new ProblemBuilder(description);
	}

	public CommandBuilder withExtensionCommand(ExtensionCommand extensionCommand) {
		throw new IllegalStateException();
	}

	public CommandBuilder withCommitCommand() {
		throw new IllegalStateException();
	}

	public CommandBuilder withResultCommand(ResultCommand resultCommand) {
		throw new IllegalStateException();
	}

	public CommandBuilder withFinishCommand() {
		throw new IllegalStateException();
	}

}
