package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.NotCommitCommand;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ResultCommand;

public class MemnodeMapping {

	private Problem problem;
	private NotCommitCommand notCommitCommand;
	private List<ResultCommand> resultCommands = new ArrayList<ResultCommand>();
	private Map<MemnodeReference, List<Command>> mapping = new HashMap<MemnodeReference, List<Command>>();
	private byte[] minitransactionId;

	public MemnodeMapping(byte[] minitransactionId) {
		this.minitransactionId = minitransactionId;
	}

	public boolean hasProblem() {
		return this.problem != null;
	}

	public Problem getProblem() {
		return problem;
	}

	public boolean hasNotCommitCommand() {
		return this.notCommitCommand != null;
	}

	public List<ResultCommand> getResultCommands() {
		return resultCommands;
	}

	public MemnodeMapping replaceCommands(Command command) {
		MemnodeMapping newMapping = new MemnodeMapping(this.minitransactionId);
		
		for(MemnodeReference reference : mapping.keySet()) {
			newMapping.add(reference, command);
		}
		
		return newMapping;
	}

	public void add(MemnodeReference reference, Command command) {
		if( command == null )
			return;
		
		if( !mapping.containsKey(reference) )
			mapping.put(reference, new ArrayList<Command>());
		
		mapping.get(reference).add(command);
		
		if( command instanceof Problem ) {
			this.problem = (Problem)command;
		}
		else if( command instanceof NotCommitCommand ) {
			this.notCommitCommand = (NotCommitCommand)command;
		}
		else if( command instanceof ResultCommand ) {
			this.resultCommands.add((ResultCommand) command);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
		result = prime * result + Arrays.hashCode(minitransactionId);
		result = prime
				* result
				+ ((notCommitCommand == null) ? 0 : notCommitCommand.hashCode());
		result = prime * result + ((problem == null) ? 0 : problem.hashCode());
		result = prime * result
				+ ((resultCommands == null) ? 0 : resultCommands.hashCode());
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
		MemnodeMapping other = (MemnodeMapping) obj;
		if (mapping == null) {
			if (other.mapping != null)
				return false;
		} else if (!mapping.equals(other.mapping))
			return false;
		if (!Arrays.equals(minitransactionId, other.minitransactionId))
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
		if (resultCommands == null) {
			if (other.resultCommands != null)
				return false;
		} else if (!resultCommands.equals(other.resultCommands))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MemnodeMapping [problem=" + problem + ", notCommitCommand="
				+ notCommitCommand + ", resultCommands=" + resultCommands
				+ ", mapping=" + mapping + ", minitransactionId="
				+ Arrays.toString(minitransactionId) + "]";
	}

	public byte[] getMinitransactionId() {
		return minitransactionId;
	}

	public Set<Entry<MemnodeReference, List<Command>>> entrySet() {
		return mapping.entrySet();
	}

	
}
