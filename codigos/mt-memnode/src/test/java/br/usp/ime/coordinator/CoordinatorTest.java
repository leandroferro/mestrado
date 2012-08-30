package br.usp.ime.coordinator;

import static br.usp.ime.Utils.bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.DummyClient;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;

public class CoordinatorTest {

	private static final Logger logger = LoggerFactory.getLogger(CoordinatorTest.class);
			
	private static final SocketAddress COORDINATOR_ADDRESS;
	static {
		try {
			COORDINATOR_ADDRESS = new InetSocketAddress(
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1234);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private static ExecutorService executor = Executors
			.newSingleThreadExecutor();
	private static Coordinator coordinator;

	@BeforeClass
	public static void startCoordinator() {
		executor.execute(new Runnable() {

			public void run() {
				coordinator = new Coordinator(COORDINATOR_ADDRESS);
				
				logger.trace("Starting {} in another thread", coordinator);
				
				coordinator.start();
			}

		});
		logger.trace("Yielding to allow another thread to run");
		Thread.yield();
	}

	@AfterClass
	public static void stopCoordinator() {
		coordinator.stop();
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.trace("Interrupted while waiting for termination", e);
		}
	}

	private static final DummyClient client = new DummyClient(
			COORDINATOR_ADDRESS);

	@Before
	public void connect() {
		logger.trace("Connecting...");
		client.connect();
		logger.trace("Connected!");
	}

	@After
	public void disconnect() {
		logger.trace("Disconnecting...");
		client.disconnect();
		logger.trace("Disconnected!");
	}

	@Test(timeout=5000)
	public void testExecuteEmptyMinitransaction() throws UnknownHostException {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}

}
