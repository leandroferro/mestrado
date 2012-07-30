package br.usp.ime.memnode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Minitransaction {

	private final byte[] id;
	
	private final List<CmpCommand> cmpCommands = new ArrayList<CmpCommand>();
	
	private final List<ReadCommand> readCommands = new ArrayList<ReadCommand>();
	
	private final List<WriteCommand> writeCommands = new ArrayList<WriteCommand>();

	public Minitransaction(byte[] id, List<CmpCommand> cmpCommands,
			List<ReadCommand> readCommands, List<WriteCommand> writeCommands) {
		super();
		this.id = id;
		this.cmpCommands.addAll( cmpCommands );
		this.readCommands.addAll( readCommands );
		this.writeCommands.addAll( writeCommands );
	}

	public Minitransaction(byte[] id) {
		this(id, Collections.<CmpCommand>emptyList(), Collections.<ReadCommand>emptyList(), Collections.<WriteCommand>emptyList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cmpCommands == null) ? 0 : cmpCommands.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((readCommands == null) ? 0 : readCommands.hashCode());
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
		if (cmpCommands == null) {
			if (other.cmpCommands != null)
				return false;
		} else if (!cmpCommands.equals(other.cmpCommands))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (readCommands == null) {
			if (other.readCommands != null)
				return false;
		} else if (!readCommands.equals(other.readCommands))
			return false;
		if (writeCommands == null) {
			if (other.writeCommands != null)
				return false;
		} else if (!writeCommands.equals(other.writeCommands))
			return false;
		return true;
	}

	public void add(ReadCommand readCommand) {
		readCommands.add(readCommand);
	}

	
}
