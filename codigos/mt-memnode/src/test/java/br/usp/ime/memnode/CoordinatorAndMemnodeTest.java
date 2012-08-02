package br.usp.ime.memnode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CoordinatorAndMemnodeTest {

	private Memnode memnode;
	private Coordinator coordinator;
	private ServerSocket serverSocket;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	@Before
	public void setUp() throws UnknownHostException, IOException {
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 6970));
		
		memnode = new Memnode(serverSocket);
		executorService.execute(new Runnable() {
			
			public void run() {
				memnode.start();
			}
		});
		
		coordinator = new Coordinator(null);
		coordinator.addMemnodeReference( new MemnodeReference(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 6970) );
	}
	
	@After
	public void tearDown() throws IOException {
		memnode.stop();
		serverSocket.close();
	}
	
	@Test
	public void coordinatorShouldReturnReadResultsWhenNoComparisonIsPresent() {
		
		Minitransaction minitransaction = new Minitransaction(new byte[]{123}, null, Arrays.asList(new ReadCommand(new byte[]{123})), null);
		
		coordinator.execute(minitransaction);
	}
}
