package br.usp.ime.protocol.command;

import java.util.Arrays;

public class Problem implements Command {

	public static final Problem CANNOT_COMMIT = new Problem("Cannot commit transaction".getBytes());
	
	private final byte[] description;

	public Problem(byte[] description) {
		this.description = description;
	}

	public byte[] getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(description);
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
		Problem other = (Problem) obj;
		if (!Arrays.equals(description, other.description))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Problem [description=" + new String(description) + "]";
	}

}
