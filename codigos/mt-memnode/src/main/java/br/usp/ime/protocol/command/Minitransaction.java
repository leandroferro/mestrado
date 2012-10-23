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

	private final NotCommitCommand notCommitCommand;

	private final TryAgainCommand tryAgainCommand;

	public Minitransaction(byte[] id, Problem problem,
			List<ReadCommand> readCommands, List<WriteCommand> writeCommands,
			List<ExtensionCommand> extensionCommands,
			List<ResultCommand> resultCommands, CommitCommand commitCommand, NotCommitCommand notCommitCommand,
			FinishCommand finishCommand, AbortCommand abortCommand, TryAgainCommand tryAgainCommand) {
		this.id = id;
		this.problem = problem;
		this.readCommands = readCommands;
		this.writeCommands = writeCommands;
		this.extensionCommands = extensionCommands;
		this.resultCommands = resultCommands;
		this.commitCommand = commitCommand;
		this.notCommitCommand = notCommitCommand;
		this.finishCommand = finishCommand;
		this.abortCommand = abortCommand;
		this.tryAgainCommand = tryAgainCommand;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((abortCommand == null) ? 0 : abortCommand.hashCode());
		result = prime * result
				+ ((commitCommand == null) ? 0 : commitCommand.hashCode());
		result = prime
				* result
				+ ((extensionCommands == null) ? 0 : extensionCommands
						.hashCode());
		result = prime * result
				+ ((finishCommand == null) ? 0 : finishCommand.hashCode());
		result = prime * result + Arrays.hashCode(id);
		result = prime
				* result
				+ ((notCommitCommand == null) ? 0 : notCommitCommand.hashCode());
		result = prime * result + ((problem == null) ? 0 : problem.hashCode());
		result = prime * result
				+ ((readCommands == null) ? 0 : readCommands.hashCode());
		result = prime * result
				+ ((resultCommands == null) ? 0 : resultCommands.hashCode());
		result = prime * result
				+ ((tryAgainCommand == null) ? 0 : tryAgainCommand.hashCode());
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
		if (abortCommand == null) {
			if (other.abortCommand != null)
				return false;
		} else if (!abortCommand.equals(other.abortCommand))
			return false;
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
		if (notCommitCommand == null) {
			if (other.notCommitCommand != null)
				return false;
		} else if (!notCommitCommand.equals(other.notCommitCommand))
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
		if (tryAgainCommand == null) {
			if (other.tryAgainCommand != null)
				return false;
		} else if (!tryAgainCommand.equals(other.tryAgainCommand))
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
				+ finishCommand + ", abortCommand=" + abortCommand
				+ ", notCommitCommand=" + notCommitCommand
				+ ", tryAgainCommand=" + tryAgainCommand + "]";
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

	public boolean hasActionCommands() {
		return readCommands.size() > 0 || writeCommands.size() > 0 || extensionCommands.size() > 0;
	}

	public Command getNotCommitCommand() {
		return notCommitCommand;
	}

	public boolean hasWriteCommands() {
		return writeCommands.size() > 0;
	}

	public TryAgainCommand getTryAgainCommand() {
		return tryAgainCommand;
	}

}
