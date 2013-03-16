package br.usp.ime.memnode;

import static br.usp.ime.Utils.bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
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

	private static final Logger logger = LoggerFactory
			.getLogger(MemnodeTest.class);

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
	private static final byte[] ABORTED_MT_ID = bytes("<<ABORTED_MT_ID>>");
	private static final byte[] ABORTED_MT_ID_2 = bytes("<<ABORTED_MT_ID_2>>");
	private static final byte[] CHAVE = bytes("<<CHAVE>>");
	private static final byte[] VALOR = bytes("<<VALOR>>");
	private static final byte[] OUTRO_VALOR = bytes("<<OUTRO_VALOR>>");
	private static final byte[] CHAVE_NAO_EXISTE = bytes("<<CHAVE_NAO_EXISTE>>");
	private static final byte[] VALOR_PARA_CHAVE_NAO_EXISTE = bytes("<<VALOR_PARA_CHAVE_NAO_EXISTE>>");

	private static final ExecutorService executor = Executors
			.newSingleThreadExecutor();

	private static Memnode memnode;

	private static DataStore dataStore;

	private static LockManager lockManager;

	@BeforeClass
	public static void startCoordinator() throws InterruptedException {

		dataStore = Mockito.mock(DataStore.class);
		lockManager = Mockito.mock(LockManager.class);

		executor.execute(new Runnable() {

			public void run() {
				memnode = new Memnode(MEMNODE_ADDRESS, dataStore, lockManager);
				logger.trace("Starting {} in another thread", memnode);
				memnode.start();
			}

		});
		logger.trace("Yielding to allow another thread to run");
		Thread.sleep(5000);
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

	private static final DummyClient client = new DummyClient(MEMNODE_ADDRESS);



	@Before
	public void connect() {
		logger.trace("Connecting...");
		client.connect();
		logger.trace("Connected!");
		Mockito.reset(dataStore, lockManager);
	}

	@After
	public void disconnect() {
		logger.trace("Disconnecting...");
		client.disconnect();
		logger.trace("Disconnected!");
	}

	@Test(timeout = 5000)
	public void testExecuteEmptyMinitransaction() throws UnknownHostException {
		for (byte[] id : Arrays.asList(MT_ID, MT_ID_2, MT_ID_3)) {
			Command minitransaction = CommandBuilder.minitransaction(id)
					.build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(id)
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);
		}
	}

	@Test(timeout = 5000)
	public void shouldReportAbortedTransaction() throws UnknownHostException {
		{
			Command minitransaction = CommandBuilder.minitransaction(ABORTED_MT_ID).build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(ABORTED_MT_ID)
					.withCommitCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(ABORTED_MT_ID).withAbortCommand().build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(ABORTED_MT_ID)
					.withAbortCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(ABORTED_MT_ID).build();
			
			client.send(minitransaction);
			
			Command expected = CommandBuilder.minitransaction(ABORTED_MT_ID)
					.withAbortCommand().build();
			Command actual = client.receive();
			
			Assert.assertEquals(expected, actual);
		}
	}

	private static ByteArrayWrapper baw(byte[] a) {
		return new ByteArrayWrapper(a);
	}

	@Test(timeout = 5000)
	public void testExecuteReadCommands() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);
		Mockito.when(
				lockManager.acquire(baw(MT_ID),
						Arrays.asList(baw(CHAVE), baw(CHAVE_NAO_EXISTE)),
						Collections.<ByteArrayWrapper> emptyList()))
				.thenReturn(true);

		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID)
					.withReadCommand(new ReadCommand(CHAVE))
					.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(MT_ID)
					.withCommitCommand()
					.withResultCommand(new ResultCommand(CHAVE, VALOR)).build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);
		}

		{
			// valido que transacoes com soment comandos de leitura terao os
			// locks liberados - era um bug
			Command minitransaction = CommandBuilder.minitransaction(MT_ID)
					.withFinishCommand().build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(MT_ID)
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);

			Mockito.verify(lockManager).release(baw(MT_ID));
		}
	}

	@Test(timeout = 5000)
	public void shouldWriteJustAfterCommit() throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);
		{
			Mockito.when(
					lockManager.acquire(baw(MT_ID),
							Collections.<ByteArrayWrapper> emptyList(),
							Arrays.asList(baw(CHAVE), baw(CHAVE_NAO_EXISTE))))
					.thenReturn(true);

			Command minitransaction = CommandBuilder
					.minitransaction(MT_ID)
					.withWriteCommand(new WriteCommand(CHAVE, OUTRO_VALOR))
					.withWriteCommand(
							new WriteCommand(CHAVE_NAO_EXISTE,
									VALOR_PARA_CHAVE_NAO_EXISTE)).build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(MT_ID)
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);

			Mockito.verify(dataStore, Mockito.never())
					.write(CHAVE, OUTRO_VALOR);
			Mockito.verify(dataStore, Mockito.never()).write(CHAVE_NAO_EXISTE,
					VALOR_PARA_CHAVE_NAO_EXISTE);
		}
		{
			Mockito.when(
					lockManager.acquire(baw(MT_ID_2),
							Arrays.asList(baw(CHAVE), baw(CHAVE_NAO_EXISTE)),
							Collections.<ByteArrayWrapper> emptyList()))
					.thenReturn(false);

			Command minitransaction = CommandBuilder.minitransaction(MT_ID_2)
					.withReadCommand(new ReadCommand(CHAVE))
					.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(MT_ID_2)
					.withTryAgainCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);
		}
		{
			Command minitransaction = CommandBuilder.minitransaction(MT_ID)
					.withFinishCommand().build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(MT_ID)
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);

			Mockito.verify(dataStore).write(CHAVE, OUTRO_VALOR);
			Mockito.verify(dataStore).write(CHAVE_NAO_EXISTE,
					VALOR_PARA_CHAVE_NAO_EXISTE);

			Mockito.verify(lockManager).release(baw(MT_ID));
		}
		{
			Mockito.when(
					lockManager.acquire(baw(MT_ID_2),
							Arrays.asList(baw(CHAVE), baw(CHAVE_NAO_EXISTE)),
							Collections.<ByteArrayWrapper> emptyList()))
					.thenReturn(true);

			Command minitransaction = CommandBuilder.minitransaction(MT_ID_2)
					.withReadCommand(new ReadCommand(CHAVE))
					.withReadCommand(new ReadCommand(CHAVE_NAO_EXISTE)).build();

			Mockito.when(dataStore.read(CHAVE)).thenReturn(OUTRO_VALOR);
			Mockito.when(dataStore.read(CHAVE_NAO_EXISTE)).thenReturn(
					VALOR_PARA_CHAVE_NAO_EXISTE);

			client.send(minitransaction);

			Command expected = CommandBuilder
					.minitransaction(MT_ID_2)
					.withResultCommand(new ResultCommand(CHAVE, OUTRO_VALOR))
					.withResultCommand(
							new ResultCommand(CHAVE_NAO_EXISTE,
									VALOR_PARA_CHAVE_NAO_EXISTE))
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);
		}
	}

	@Test(timeout = 5000)
	public void shouldReleaseLocksAfterAbort() throws UnknownHostException {
		Command minitransaction = CommandBuilder.minitransaction(ABORTED_MT_ID_2)
				.withAbortCommand().build();

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(ABORTED_MT_ID_2)
				.withAbortCommand().build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);

		Mockito.verify(lockManager).release(baw(ABORTED_MT_ID_2));
	}

	@Test(timeout = 5000)
	public void testExecuteEqualsExtensionCommands()
			throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);
		Mockito.when(
				lockManager.acquire(baw(MT_ID), Arrays.asList(baw(CHAVE)),
						Collections.<ByteArrayWrapper> emptyList()))
				.thenReturn(true);

		Command minitransaction = CommandBuilder
				.minitransaction(MT_ID)
				.withExtensionCommand(
						new ExtensionCommand(bytes("ECMP"), Arrays.asList(
								new Param(CHAVE), new Param(VALOR)))).build();

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(MT_ID)
				.withCommitCommand().build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
	}

	@Test(timeout = 5000)
	public void testExecuteEqualsExtensionCommandsWhenComparisonFails()
			throws UnknownHostException {
		logger.trace("Populating dataStore...");
		Mockito.when(dataStore.read(CHAVE)).thenReturn(VALOR);
		Mockito.when(
				lockManager.acquire(baw(MT_ID), Arrays.asList(baw(CHAVE)),
						Collections.<ByteArrayWrapper> emptyList()))
				.thenReturn(true);

		Command minitransaction = CommandBuilder
				.minitransaction(MT_ID)
				.withExtensionCommand(
						new ExtensionCommand(bytes("ECMP"), Arrays.asList(
								new Param(CHAVE), new Param(OUTRO_VALOR))))
				.build();

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(MT_ID)
				.withProblem(new Problem(bytes("ABORT"))).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
	}

	@Test(timeout = 5000)
	public void testAbortWhenReadFailsForComparison()
			throws UnknownHostException {
		Mockito.when(dataStore.read(CHAVE)).thenThrow(RuntimeException.class);
		Mockito.when(
				lockManager.acquire(baw(MT_ID), Arrays.asList(baw(CHAVE)),
						Collections.<ByteArrayWrapper> emptyList()))
				.thenReturn(true);

		Command minitransaction = CommandBuilder
				.minitransaction(MT_ID)
				.withExtensionCommand(
						new ExtensionCommand(bytes("ECMP"), Arrays.asList(
								new Param(CHAVE), new Param(OUTRO_VALOR))))
				.build();

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(MT_ID)
				.withProblem(new Problem(bytes("ABORT"))).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
	}
}
