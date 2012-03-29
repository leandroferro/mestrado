package node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class Minitransaction {

	private final String id;
	private final List<CompareItem> compareList = new ArrayList<CompareItem>();
	private final List<ReadItem> readList = new ArrayList<ReadItem>();
	private final List<WriteItem> writeList = new ArrayList<WriteItem>();
	private final List<ReadItem> allReadList = new ArrayList<ReadItem>();
	
	public Minitransaction(String id) {
		this.id = id;
	}
	
	public void add(CompareItem compareItem) {
		compareList.add(compareItem);
		allReadList.add(compareItem);
	}
	
	public void add(ReadItem readItem) {
		readList.add(readItem);
		allReadList.add(readItem);
	}
	
	public void add(WriteItem writeItem) {
		writeList.add(writeItem);
	}

	public Iterator<WriteItem> getWriteIterator() {
		return writeList.iterator();
	}

	public Iterator<ReadItem> getReadIterator() {
		return readList.iterator();
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Minitransaction [id=" + id + ", compareList=" + compareList
				+ ", readList=" + readList + ", writeList=" + writeList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((compareList == null) ? 0 : compareList.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((readList == null) ? 0 : readList.hashCode());
		result = prime * result
				+ ((writeList == null) ? 0 : writeList.hashCode());
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
		if (compareList == null) {
			if (other.compareList != null)
				return false;
		} else if (!compareList.equals(other.compareList))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (readList == null) {
			if (other.readList != null)
				return false;
		} else if (!readList.equals(other.readList))
			return false;
		if (writeList == null) {
			if (other.writeList != null)
				return false;
		} else if (!writeList.equals(other.writeList))
			return false;
		return true;
	}

	public Iterator<CompareItem> getCompareIterator() {
		return compareList.iterator();
	}

	public List<ReadItem> getAllReads() {
		return Collections.unmodifiableList(allReadList);
	}

	
}
