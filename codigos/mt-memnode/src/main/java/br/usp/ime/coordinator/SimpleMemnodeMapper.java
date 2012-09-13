package br.usp.ime.coordinator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleMemnodeMapper implements MemnodeMapper {

	private final List<MemnodeReference> references = new ArrayList<MemnodeReference>();
	
	public void add(MemnodeReference reference) {
		references.add(reference);
	}
	
	public MemnodeReference map(byte[] bytes) {
		return references.get( Arrays.hashCode(bytes) % references.size() );
	}

}
