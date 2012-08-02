package br.usp.ime.memnode;

public class FailedProcessingResult extends ProcessingResult {

	private final String problemDescription;

	public FailedProcessingResult(Minitransaction minitransaction,
			String problemDescription) {
		super(minitransaction);
		this.problemDescription = problemDescription;
	}

	public String getProblemDescription() {
		return problemDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((problemDescription == null) ? 0 : problemDescription
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FailedProcessingResult other = (FailedProcessingResult) obj;
		if (problemDescription == null) {
			if (other.problemDescription != null)
				return false;
		} else if (!problemDescription.equals(other.problemDescription))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FailedProcessingResult [problemDescription="
				+ problemDescription + "]";
	}

}
