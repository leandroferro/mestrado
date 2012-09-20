package br.usp.ime.coordinator;

public interface ConnectionProvider {

	Connection newConnection(MemnodeReference memnodeReference);

}
