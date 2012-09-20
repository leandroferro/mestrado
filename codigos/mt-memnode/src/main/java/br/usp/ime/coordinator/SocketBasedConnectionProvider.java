package br.usp.ime.coordinator;

public class SocketBasedConnectionProvider implements ConnectionProvider {

	@Override
	public Connection newConnection(MemnodeReference memnodeReference) {

		return new SocketBasedConnection( memnodeReference.getAddress() );

	}

}
