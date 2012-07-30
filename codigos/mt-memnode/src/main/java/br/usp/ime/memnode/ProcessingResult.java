package br.usp.ime.memnode;

public class ProcessingResult {

	private final Minitransaction minitransaction;

	public ProcessingResult(Minitransaction minitransaction) {
		this.minitransaction = minitransaction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((minitransaction == null) ? 0 : minitransaction.hashCode());
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
		ProcessingResult other = (ProcessingResult) obj;
		if (minitransaction == null) {
			if (other.minitransaction != null)
				return false;
		} else if (!minitransaction.equals(other.minitransaction))
			return false;
		return true;
	}

	
}
