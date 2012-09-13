package br.usp.ime.coordinator;

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
	
	private static MemnodeDispatcher dispatcher;

	@BeforeClass
	public static void startCoordinator() {
		
		dispatcher = Mockito.mock(MemnodeDispatcher.class);
		
		coordinator = new Coordinator(COORDINATOR_ADDRESS, dispatcher);
		
		executor.execute(new Runnable() {

			public void run() {
				
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
		
		Mockito.reset(dispatcher);
	}

	@After
	public void disconnect() {
		logger.trace("Disconnecting...");
		client.disconnect();
		logger.trace("Disconnected!");
	}

	@Test(timeout=5000)
	public void testExecuteEmptyMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.build();

		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
	}

	@Test(timeout=5000)
	public void testCommitReadOnlyMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>"))).withReadCommand(new ReadCommand(bytes("<<CHAVE_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"),  bytes("<<DATA_1>>"))).withResultCommand(new ResultCommand(bytes("<<CHAVE_2>>"), bytes("<<DATA_2>>"))).build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"),  bytes("<<DATA_1>>"))).withResultCommand(new ResultCommand(bytes("<<CHAVE_2>>"), bytes("<<DATA_2>>"))).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withFinishCommand().build());
		
	}
	
	@Test(timeout=5000)
	public void testReportProblemWithReadOnlyMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>"))).withReadCommand(new ReadCommand(bytes("<<CHAVE_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"),  bytes("<<DATA_1>>"))).withProblem(new Problem(bytes("<<ERRO>>"))).build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withProblem(new Problem(bytes("<<ERRO>>"))).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withAbortCommand().build());
	
	}
	
	@Test(timeout=5000)
	public void testCommitWriteMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withWriteCommand(new WriteCommand(bytes("<<CHAVE_1>>"), bytes("<<DADOS_1>>"))).withWriteCommand(new WriteCommand(bytes("<<CHAVE_2>>"), bytes("<<DADOS_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withCommitCommand().build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withFinishCommand().build());
	
	}
	
	@Test(timeout=5000)
	public void testAbortWriteMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withWriteCommand(new WriteCommand(bytes("<<CHAVE_1>>"), bytes("<<DADOS_1>>"))).withWriteCommand(new WriteCommand(bytes("<<CHAVE_2>>"), bytes("<<DADOS_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withCommitCommand().withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withAbortCommand().build());
	
	}
	
	@Test(timeout=5000)
	public void testCommitExtensionMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withExtensionCommand(new ExtensionCommand(bytes("ABCD"), Arrays.asList(new Param(bytes("<<PARAM_1>>")), new Param(bytes("<<PARAM_2>>"))))).withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>"))).withWriteCommand(new WriteCommand(bytes("<<CHAVE_2>>"), bytes("<<DADOS_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"), bytes("<<DADO_1>>"))).withCommitCommand().build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"), bytes("<<DADO_1>>"))).withCommitCommand().build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withFinishCommand().build());
	
	}
	
	@Test(timeout=5000)
	public void testAbortExtensionMinitransactionWhenProblem() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withExtensionCommand(new ExtensionCommand(bytes("ABCD"), Arrays.asList(new Param(bytes("<<PARAM_1>>")), new Param(bytes("<<PARAM_2>>"))))).withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>"))).withWriteCommand(new WriteCommand(bytes("<<CHAVE_2>>"), bytes("<<DADOS_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"), bytes("<<DADO_1>>"))).withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withAbortCommand().build());
	
	}
	
	@Test(timeout=5000)
	public void testAbortExtensionMinitransactionWhenNotCommit() {

		Command minitransaction = CommandBuilder.minitransaction(bytes("abc"))
				.withExtensionCommand(new ExtensionCommand(bytes("ABCD"), Arrays.asList(new Param(bytes("<<PARAM_1>>")), new Param(bytes("<<PARAM_2>>"))))).withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>"))).withWriteCommand(new WriteCommand(bytes("<<CHAVE_2>>"), bytes("<<DADOS_2>>"))).build();

		Command collected = CommandBuilder.minitransaction(bytes("abc")).withResultCommand(new ResultCommand(bytes("<<CHAVE_1>>"), bytes("<<DADO_1>>"))).withNotCommitCommand().build();
		Mockito.when(dispatcher.dispatchAndCollect(minitransaction)).thenReturn(collected);
		
		client.send(minitransaction);
		
		Command expected = CommandBuilder.minitransaction(bytes("abc")).withProblem(Problem.CANNOT_COMMIT).build();
		Command actual = client.receive();
		
		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(CommandBuilder.minitransaction(bytes("abc")).withAbortCommand().build());
	
	}

}
