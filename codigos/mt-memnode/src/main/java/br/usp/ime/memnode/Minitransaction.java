package br.usp.ime.memnode;

import java.util.ArrayList;
import java.util.Arrays;
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
		this.cmpCommands.addAll(cmpCommands);
		this.readCommands.addAll(readCommands);
		this.writeCommands.addAll(writeCommands);
	}

	public Minitransaction(byte[] id) {
		this(id, Collections.<CmpCommand> emptyList(), Collections
				.<ReadCommand> emptyList(), Collections
				.<WriteCommand> emptyList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(id);
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
		if (!Arrays.equals(id, other.id))
			return false;
		return true;
	}

	public void add(ReadCommand readCommand) {
		readCommands.add(readCommand);
	}

	public byte[] getId() {
		return id;
	}

	public List<ReadCommand> getReadCommands() {
		return Collections.unmodifiableList(readCommands);
	}

}
