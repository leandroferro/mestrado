package br.usp.ime.coordinator;

import static br.usp.ime.Utils.bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
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
import br.usp.ime.protocol.command.AbortCommand;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.CommitCommand;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.FinishCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.NotCommitCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class CoordinatorTest {

	private static final String MT_ID = "<<ID>>";
	private static final String MT_ID_2 = "<<ID_2>>";
	private static final String MT_ID_3 = "<<ID_3>>";

	private static final Logger logger = LoggerFactory
			.getLogger(CoordinatorTest.class);

	private static final InetSocketAddress COORDINATOR_ADDRESS;
	private static final InetSocketAddress ADDR_REFERENCE_1;
	private static final InetSocketAddress ADDR_REFERENCE_2;
	static {
		try {
			COORDINATOR_ADDRESS = new InetSocketAddress(
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1234);

			ADDR_REFERENCE_1 = new InetSocketAddress(
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1235);

			ADDR_REFERENCE_2 = new InetSocketAddress(
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1236);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private static ExecutorService executor = Executors
			.newSingleThreadExecutor();

	private static Coordinator coordinator;

	private static MemnodeDispatcher dispatcher;

	private static MemnodeMapper mapper;

	private static final MemnodeReference REFERENCE_1 = new MemnodeReference(
			ADDR_REFERENCE_1);
	private static final MemnodeReference REFERENCE_2 = new MemnodeReference(
			ADDR_REFERENCE_2);

	@BeforeClass
	public static void startCoordinator() {

		mapper = Mockito.mock(MemnodeMapper.class);

		dispatcher = Mockito.mock(MemnodeDispatcher.class);

		coordinator = new Coordinator(COORDINATOR_ADDRESS, mapper, dispatcher);

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

	@Test(timeout = 5000)
	public void testExecuteEmptyMinitransaction() {
		for (String id : Arrays.asList(MT_ID, MT_ID_2, MT_ID_3)) {
			Command minitransaction = CommandBuilder.minitransaction(bytes(id))
					.build();

			client.send(minitransaction);

			Command expected = CommandBuilder.minitransaction(bytes(id))
					.withCommitCommand().build();
			Command actual = client.receive();

			Assert.assertEquals(expected, actual);
		}
	}

	@Test(timeout = 5000)
	public void testCommitReadOnlyMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes(MT_ID))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_2>>"))).build();

		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new ReadCommand(bytes("<<CHAVE_1>>")));
		mapping.add(REFERENCE_2, new ReadCommand(bytes("<<CHAVE_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, new ResultCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DATA_1>>")));
		collectedMapping.add(REFERENCE_2, new ResultCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DATA_2>>")));

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, FinishCommand.instance());
		finishMapping.add(REFERENCE_2, FinishCommand.instance());

		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withResultCommand(
						new ResultCommand(bytes("<<CHAVE_1>>"),
								bytes("<<DATA_1>>")))
				.withResultCommand(
						new ResultCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DATA_2>>"))).withCommitCommand()
				.build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);

	}

	@Test(timeout = 5000)
	public void testReportProblemWithReadOnlyMinitransaction() {

		Command minitransaction = CommandBuilder.minitransaction(bytes(MT_ID))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_2>>"))).build();
		
		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new ReadCommand(bytes("<<CHAVE_1>>")));
		mapping.add(REFERENCE_2, new ReadCommand(bytes("<<CHAVE_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, new ResultCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DATA_1>>")));
		collectedMapping.add(REFERENCE_2, new Problem(bytes("<<ERRO>>")));

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, AbortCommand.instance());
		finishMapping.add(REFERENCE_2, AbortCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(bytes(MT_ID))
				.withProblem(new Problem(bytes("<<ERRO>>"))).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(
				finishMapping);

	}

	@Test(timeout = 5000)
	public void testCommitWriteMinitransaction() {
		Command minitransaction = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_1>>"),
								bytes("<<DADOS_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_2>>"))).build();

		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new WriteCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DADOS_1>>")));
		mapping.add(REFERENCE_2, new WriteCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DADOS_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, CommitCommand.instance());
		collectedMapping.add(REFERENCE_2, CommitCommand.instance());

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, FinishCommand.instance());
		finishMapping.add(REFERENCE_2, FinishCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(bytes(MT_ID))
				.withCommitCommand().build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);
	}

	@Test(timeout = 5000)
	public void testAbortWriteMinitransaction() {

		Command minitransaction = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_1>>"),
								bytes("<<DADOS_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_2>>"))).build();

		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new WriteCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DADOS_1>>")));
		mapping.add(REFERENCE_2, new WriteCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DADOS_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, CommitCommand.instance());
		collectedMapping.add(REFERENCE_2, new Problem(bytes("<<PROBLEMA>>")));

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, AbortCommand.instance());
		finishMapping.add(REFERENCE_2, AbortCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);
		
		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(bytes(MT_ID))
				.withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);

	}

	@Test(timeout = 5000)
	public void testCommitExtensionMinitransaction() {

		Command minitransaction = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays.asList(
								new Param(bytes("<<PARAM_1>>")), new Param(
										bytes("<<PARAM_2>>")))))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_2>>"))).build();


		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new ExtensionCommand(bytes("ABCD"), Arrays.asList(
				new Param(bytes("<<PARAM_1>>")), new Param(
						bytes("<<PARAM_2>>")))));
		mapping.add(REFERENCE_2, new ReadCommand(bytes("<<CHAVE_1>>")));
		mapping.add(REFERENCE_1, new WriteCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DADOS_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, CommitCommand.instance());
		collectedMapping.add(REFERENCE_2, new ResultCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DADO_1>>")));
		collectedMapping.add(REFERENCE_2, CommitCommand.instance());

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, FinishCommand.instance());
		finishMapping.add(REFERENCE_2, FinishCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withResultCommand(
						new ResultCommand(bytes("<<CHAVE_1>>"),
								bytes("<<DADO_1>>"))).withCommitCommand()
				.build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);

	}

	@Test(timeout = 5000)
	public void testAbortExtensionMinitransactionWhenProblem() {

		Command minitransaction = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays.asList(
								new Param(bytes("<<PARAM_1>>")), new Param(
										bytes("<<PARAM_2>>")))))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_2>>"))).build();

	
		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new ExtensionCommand(bytes("ABCD"), Arrays.asList(
				new Param(bytes("<<PARAM_1>>")), new Param(
						bytes("<<PARAM_2>>")))));
		mapping.add(REFERENCE_2, new ReadCommand(bytes("<<CHAVE_1>>")));
		mapping.add(REFERENCE_1, new WriteCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DADOS_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, new Problem(bytes("<<PROBLEMA>>")));
		collectedMapping.add(REFERENCE_2, new ResultCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DADO_1>>")));
		collectedMapping.add(REFERENCE_2, CommitCommand.instance());

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, AbortCommand.instance());
		finishMapping.add(REFERENCE_2, AbortCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(bytes(MT_ID))
				.withProblem(new Problem(bytes("<<PROBLEMA>>"))).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);

	}

	@Test(timeout = 5000)
	public void testAbortExtensionMinitransactionWhenNotCommit() {

		Command minitransaction = CommandBuilder
				.minitransaction(bytes(MT_ID))
				.withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays.asList(
								new Param(bytes("<<PARAM_1>>")), new Param(
										bytes("<<PARAM_2>>")))))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_2>>"))).build();
		
		MemnodeMapping mapping = new MemnodeMapping(bytes(MT_ID));
		mapping.add(REFERENCE_1, new ExtensionCommand(bytes("ABCD"), Arrays.asList(
				new Param(bytes("<<PARAM_1>>")), new Param(
						bytes("<<PARAM_2>>")))));
		mapping.add(REFERENCE_2, new ReadCommand(bytes("<<CHAVE_1>>")));
		mapping.add(REFERENCE_1, new WriteCommand(bytes("<<CHAVE_2>>"),
				bytes("<<DADOS_2>>")));

		MemnodeMapping collectedMapping = new MemnodeMapping(bytes(MT_ID));
		collectedMapping.add(REFERENCE_1, NotCommitCommand.instance());
		collectedMapping.add(REFERENCE_2, new ResultCommand(bytes("<<CHAVE_1>>"),
				bytes("<<DADO_1>>")));
		collectedMapping.add(REFERENCE_2, CommitCommand.instance());

		MemnodeMapping finishMapping = new MemnodeMapping(bytes(MT_ID));
		finishMapping.add(REFERENCE_1, AbortCommand.instance());
		finishMapping.add(REFERENCE_2, AbortCommand.instance());
		
		Mockito.when(mapper.map((Minitransaction) minitransaction)).thenReturn(
				mapping);

		Mockito.when(dispatcher.dispatchAndCollect(mapping)).thenReturn(
				collectedMapping);

		client.send(minitransaction);

		Command expected = CommandBuilder.minitransaction(bytes(MT_ID))
				.withProblem(Problem.CANNOT_COMMIT).build();
		Command actual = client.receive();

		Assert.assertEquals(expected, actual);
		Mockito.verify(dispatcher).dispatch(finishMapping);

	}

}
