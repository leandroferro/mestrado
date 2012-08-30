package br.usp.ime.memnode;

public interface DataStore {

	byte[] read(byte[] key);

	void write(byte[] key, byte[] data);

	void remove(byte[] key);

}
