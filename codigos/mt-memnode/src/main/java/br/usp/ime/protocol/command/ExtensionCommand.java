package br.usp.ime.protocol.command;

import java.util.Arrays;
import java.util.List;

public class ExtensionCommand implements Command {

	private final byte[] id;
	private final List<Param> params;

	public ExtensionCommand(byte[] id, List<Param> params) {
		this.id = id;
		this.params = params;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(id);
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		ExtensionCommand other = (ExtensionCommand) obj;
		if (!Arrays.equals(id, other.id))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExtensionCommand [id=" + Arrays.toString(id) + ", params="
				+ params + "]";
	}

	public byte[] getId() {
		return id;
	}

	public List<Param> getParams() {
		return params;
	}

}
