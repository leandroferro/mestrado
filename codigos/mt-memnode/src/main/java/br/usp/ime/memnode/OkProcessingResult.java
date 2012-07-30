package br.usp.ime.memnode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OkProcessingResult extends ProcessingResult {

	private final List<ReadResultItem> readResultItems = new ArrayList<ReadResultItem>();
	
	public OkProcessingResult(Minitransaction minitransaction) {
		super(minitransaction);
	}

	public OkProcessingResult(Minitransaction minitransaction,
			List<ReadResultItem> readResultItems) {
		super(minitransaction);
		this.readResultItems.addAll(readResultItems);
	}

	public OkProcessingResult(Minitransaction minitransaction,
			ReadResultItem ...readResultItems) {
		this(minitransaction, Arrays.asList(readResultItems));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((readResultItems == null) ? 0 : readResultItems.hashCode());
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
		OkProcessingResult other = (OkProcessingResult) obj;
		if (readResultItems == null) {
			if (other.readResultItems != null)
				return false;
		} else if (!readResultItems.equals(other.readResultItems))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OkProcessingResult [readResultItems=" + readResultItems + "]";
	}

}
