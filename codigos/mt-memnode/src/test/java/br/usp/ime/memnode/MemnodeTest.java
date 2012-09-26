package br.usp.ime.memnode;

import static br.usp.ime.Utils.bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.DummyClient;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class MemnodeTest {

	private static final Logger logger = LoggerFactory.getLogger(MemnodeTest.class);
			
	private static final SocketAddress MEMNODE_ADDRESS;
	static {
		try {
			MEMNODE_ADDRESS = new InetSocketAddress(
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1235);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private static final byte[] MT_ID = bytes("<<MT_ID>>");
	private static final byte[] MT_ID_2 = bytes("<<MT_ID_2>>");
	private static final byte[] MT_ID_3 = bytes("<<MT_ID_3>>");
	private static final byte[] CHAVE = bytes("<<CHAVE>>");
	private static final byte[] VALOR = bytes("<<VALOR>>");
	private static final byte[] OUTRO_VALOR = bytes("<<OUTRO_VALOR>>");
	private static final byte[] CHAVE_NAO_EXISTE = bytes("<<CHAVE_NAO_EXISTE>>");
	private static final byte[] VALOR_PARA_CHAVE_NAO_EXISTE = bytes("<<VALOR_PARA_CHAVE_NAO_EXISTE>>");
	
	private static final ExecutorService executor = Executors
			.newSingleThreadExecutor();
	
	private static Memnode memnode;
	
	private static DataStore dataStore;
	

	@BeforeClass
	public static void startCoordinator() {
		dataStore = Mockito.mock(DataStore.class);
		
		executor.execute(new Runnable() {

			public void run() {
				memnode = new Memnode(MEMNODE_ADDRESS, dataStore);
				logger.trace("Starting {} in another thread", memnode);
				memnode.start();
			}

		});
		logger.trace("Yielding to allow another thread to run");
		Thread.yield();
	}

	@AfterClass
	public static void stopCoordinator() {
		memnode.stop();
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.trace("Interrupted while waiting for termination", e);
		}
	}

	private static final DummyClient client = new DummyClient(
			MEMNODE_ADDRESS);



	@Before
	public void connect() {
		logger.trace("Connecting...");
		client.connect();
		logger.trace("Connected!");
		Mockito.reset(dataStore);
	}

	@After
	public void disconnect() {
		logger.trace("Disconnecting...");
		client.disconnect();
		logger.trace("Disconnected!");
	}

	@Test(timeout=5000)
	public void testExecuteEmptyMinitransaction() throws UnknownHostException {
		for(byte[] id : Arrays.asList(MT_ID, MT_ID_2, MT_ID_3)){
			Command minitransaction = CommandBuilder.minitransaction(id)
					.build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(id).withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
	}
	
	@Test(timeout=5000)
	public void testExecuteReadCommands() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);

		Command minitransaction = CommandBuilder.minitransaction(MT_ID).withReadCommand(new ReadCommand(CHAVE))
				.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(MT_ID).withCommitCommand().withResultCommand(new ResultCommand(CHAVE, VALOR)).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}

	@Test()
	public void shouldWriteJustAfterCommit() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);
		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID).withWriteCommand(new WriteCommand(CHAVE, OUTRO_VALOR))
					.withWriteCommand(new WriteCommand(CHAVE_NAO_EXISTE, VALOR_PARA_CHAVE_NAO_EXISTE)).build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(MT_ID).withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
			
			Mockito.verify(dataStore, Mockito.never()).write(CHAVE, OUTRO_VALOR);
			Mockito.verify(dataStore, Mockito.never()).write(CHAVE_NAO_EXISTE, VALOR_PARA_CHAVE_NAO_EXISTE);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID_2).withReadCommand(new ReadCommand(CHAVE))
					.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(MT_ID_2).withResultCommand(new ResultCommand(CHAVE, VALOR)).withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID).withFinishCommand().build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(MT_ID).withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
			
			Mockito.verify(dataStore).write(CHAVE, OUTRO_VALOR);
			Mockito.verify(dataStore).write(CHAVE_NAO_EXISTE, VALOR_PARA_CHAVE_NAO_EXISTE);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID_2).withReadCommand(new ReadCommand(CHAVE))
					.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();
			
			Mockito.when(dataStore.read(CHAVE)).thenReturn(OUTRO_VALOR);
			Mockito.when(dataStore.read(CHAVE_NAO_EXISTE)).thenReturn(VALOR_PARA_CHAVE_NAO_EXISTE);
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(MT_ID_2).withResultCommand(new ResultCommand(CHAVE, OUTRO_VALOR)).withResultCommand(new ResultCommand(CHAVE_NAO_EXISTE, VALOR_PARA_CHAVE_NAO_EXISTE)).withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
	}
	
	@Test(timeout=5000)
	public void testExecuteEqualsExtensionCommands() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);

		Command minitransaction = CommandBuilder.minitransaction(MT_ID).withExtensionCommand(new ExtensionCommand(bytes("ECMP"), Arrays.asList(new Param(CHAVE), new Param(VALOR))))
				.build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(MT_ID).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test(timeout=5000)
	public void testExecuteEqualsExtensionCommandsWhenComparisonFails() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);

		Command minitransaction = CommandBuilder.minitransaction(MT_ID).withExtensionCommand(new ExtensionCommand(bytes("ECMP"), Arrays.asList(new Param(CHAVE), new Param(OUTRO_VALOR))))
				.build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(MT_ID).withProblem(new Problem(bytes("ABORT"))).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}
	
	@Test(timeout=5000)
	public void testAbortWhenReadFailsForComparison() throws UnknownHostException {
		Mockito.when(dataStore.read(CHAVE)).thenThrow(RuntimeException.class);

		Command minitransaction = CommandBuilder.minitransaction(MT_ID).withExtensionCommand(new ExtensionCommand(bytes("ECMP"), Arrays.asList(new Param(CHAVE), new Param(OUTRO_VALOR))))
				.build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(MT_ID).withProblem(new Problem(bytes("ABORT"))).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}
}
