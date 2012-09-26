package br.usp.ime.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalConnectionFactory implements ConnectionFactory {

	private static final Logger logger = LoggerFactory.getLogger(ThreadLocalConnectionFactory.class);
			
	private final ThreadLocal<MemnodeReference> references = new ThreadLocal<MemnodeReference>();
	
	private final ThreadLocal<Connection> connections = new ThreadLocal<Connection>() {

		@Override
		protected Connection initialValue() {
			MemnodeReference reference = references.get();
			logger.debug("Creating new connection for {}", reference);
			return provider.newConnection( reference );
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
