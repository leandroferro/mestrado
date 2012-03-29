package node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import server.ReadResult;

public class ExecutionResult {

	public enum Outcome {
		ABORT, COMMIT
	}
	
	private final Outcome outcome;
	private final String id;
	private final List<ReadResult> readResults;
	
	public ExecutionResult(Outcome outcome, String id,List<ReadResult> readResults) {
		super();
		this.outcome = outcome;
		this.id = id;
		this.readResults = new ArrayList<ReadResult>(readResults);
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "ExecutionResult [outcome=" + outcome + ", id=" + id
				+ ", readResults=" + readResults + "]";
	}

	public Iterator<ReadResult> getReadResultIterator() {
		return readResults.iterator();
	}

	public List<ReadResult> getReadResults() {
		return readResults;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((outcome == null) ? 0 : outcome.hashCode());
		result = prime * result
				+ ((readResults == null) ? 0 : readResults.hashCode());
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
		ExecutionResult other = (ExecutionResult) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (outcome != other.outcome)
			return false;
		if (readResults == null) {
			if (other.readResults != null)
				return false;
		} else if (!readResults.equals(other.readResults))
			return false;
		return true;
	}
	
	
}
