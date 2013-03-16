package br.usp.ime.protocol.parser;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.memnode.ByteArrayWrapper;

public class ByteArrayBuilder {

	private List<Byte> list = new ArrayList<Byte>();
	
	public ByteArrayBuilder append(byte[] bytes) {
		for(byte b : bytes) {
			list.add(b);
		}
		return this;
	}

	public ByteArrayBuilder append(int i) {
		return append( Integer.toString(i).getBytes() );
	}

	public ByteArrayBuilder append(ByteArrayWrapper serialize) {
		return append(serialize.value);
	}

	public byte[] build() {
		byte[] result = new byte[list.size()];
		for(int i=0;i < list.size();i++){
			result[i] = list.get(i);
		}
		return result;
	}

}
