package br.usp.ime.protocol.command;

import java.util.Arrays;
import java.util.List;

public class Minitransaction implements Command {

	private final byte[] id;

	private final Problem problem;

	private final List<ReadCommand> readCommands;

	private final List<WriteCommand> writeCommands;

	private final List<ExtensionCommand> extensionCommands;

	private final CommitCommand commitCommand;

	private final List<ResultCommand> resultCommands;

	private final FinishCommand finishCommand;

	private final AbortCommand abortCommand;

	public Minitransaction(byte[] id, Problem problem,
			List<ReadCommand> readCommands, List<WriteCommand> writeCommands,
			List<ExtensionCommand> extensionCommands,
			CommitCommand commitCommand, List<ResultCommand> resultCommands,
			FinishCommand finishCommand, AbortCommand abortCommand) {
		this.id = id;
		this.problem = problem;
		this.readCommands = readCommands;
		this.writeCommands = writeCommands;
		this.extensionCommands = extensionCommands;
		this.commitCommand = commitCommand;
		this.resultCommands = resultCommands;
		this.finishCommand = finishCommand;
		this.abortCommand = abortCommand;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commitCommand == null) ? 0 : commitCommand.hashCode());
		result = prime
				* result
				+ ((extensionCommands == null) ? 0 : extensionCommands
						.hashCode());
		result = prime * result
				+ ((finishCommand == null) ? 0 : finishCommand.hashCode());
		result = prime * result + Arrays.hashCode(id);
		result = prime * result + ((problem == null) ? 0 : problem.hashCode());
		result = prime * result
				+ ((readCommands == null) ? 0 : readCommands.hashCode());
		result = prime * result
				+ ((resultCommands == null) ? 0 : resultCommands.hashCode());
		result = prime * result
				+ ((writeCommands == null) ? 0 : writeCommands.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Minitransaction other = (Minitransaction) obj;
		if (commitCommand == null) {
			if (other.commitCommand != null)
				return false;
		} else if (!commitCommand.equals(other.commitCommand))
			return false;
		if (extensionCommands == null) {
			if (other.extensionCommands != null)
				return false;
		} else if (!extensionCommands.equals(other.extensionCommands))
			return false;
		if (finishCommand == null) {
			if (other.finishCommand != null)
				return false;
		} else if (!finishCommand.equals(other.finishCommand))
			return false;
		if (!Arrays.equals(id, other.id))
			return false;
		if (problem == null) {
			if (other.problem != null)
				return false;
		} else if (!problem.equals(other.problem))
			return false;
		if (readCommands == null) {
			if (other.readCommands != null)
				return false;
		} else if (!readCommands.equals(other.readCommands))
			return false;
		if (resultCommands == null) {
			if (other.resultCommands != null)
				return false;
		} else if (!resultCommands.equals(other.resultCommands))
			return false;
		if (writeCommands == null) {
			if (other.writeCommands != null)
				return false;
		} else if (!writeCommands.equals(other.writeCommands))
			return false;
		return true;
	}

	public byte[] getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Minitransaction [id=" + Arrays.toString(id) + ", problem="
				+ problem + ", readCommands=" + readCommands
				+ ", writeCommands=" + writeCommands + ", extensionCommands="
				+ extensionCommands + ", commitCommand=" + commitCommand
				+ ", resultCommands=" + resultCommands + ", finishCommand="
				+ finishCommand + "]";
	}

	public Problem getProblem() {
		return problem;
	}

	public List<ReadCommand> getReadCommands() {
		return readCommands;
	}

	public List<WriteCommand> getWriteCommands() {
		return writeCommands;
	}

	public List<ExtensionCommand> getExtensionCommands() {
		return extensionCommands;
	}

	public Command getCommitCommand() {
		return commitCommand;
	}

	public List<ResultCommand> getResultCommands() {
		return resultCommands;
	}

	public Command getFinishCommand() {
		return finishCommand;
	}

	public AbortCommand getAbortCommand() {
		return abortCommand;
	}

}
