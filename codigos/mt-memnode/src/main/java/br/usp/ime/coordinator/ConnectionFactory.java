package br.usp.ime.coordinator;

public interface ConnectionFactory {

	Connection create(MemnodeReference reference);

}
