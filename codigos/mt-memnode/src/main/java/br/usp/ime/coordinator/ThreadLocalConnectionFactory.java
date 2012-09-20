package br.usp.ime.coordinator;

public class ThreadLocalConnectionFactory implements ConnectionFactory {

	private final ThreadLocal<MemnodeReference> references = new ThreadLocal<MemnodeReference>();
	
	private final ThreadLocal<Connection> connections = new ThreadLocal<Connection>() {

		@Override
		protected Connection initialValue() {
			return provider.newConnection( references.get() );
		}
		
	};
	
	private final ConnectionProvider provider;
	
	public ThreadLocalConnectionFactory(ConnectionProvider provider) {
		this.provider = provider;
	}

	@Override
	public Connection create(MemnodeReference reference) {
		references.set(reference);
		return connections.get();
	}

	@Override
	public String toString() {
		return "ThreadLocalConnectionFactory [references=" + references
				+ ", connections=" + connections + ", provider=" + provider
				+ "]";
	}

	
}
