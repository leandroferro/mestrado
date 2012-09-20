package br.usp.ime.coordinator;

import static br.usp.ime.Utils.bytes;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class BasicMemnodeDispatcherTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldDispatchAccordingToMapper() throws Exception {

		InetSocketAddress addr1 = new InetSocketAddress(
				InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1235);

		InetSocketAddress addr2 = new InetSocketAddress(
				InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1236);

		MemnodeReference ref1 = new MemnodeReference(addr1);
		MemnodeReference ref2 = new MemnodeReference(addr2);

		MemnodeClient client = Mockito.mock(MemnodeClient.class);

		MemnodeMapper mapper = Mockito.mock(MemnodeMapper.class);

		Mockito.when(mapper.map(bytes("<<CHAVE_1>>"))).thenReturn(ref1);
		Mockito.when(mapper.map(bytes("<<CHAVE_2>>"))).thenReturn(ref2);
		Mockito.when(mapper.map(bytes("<<CHAVE_3>>"))).thenReturn(ref1);

		BasicMemnodeDispatcher dispatcher = new BasicMemnodeDispatcher(mapper, client);

		Command command = CommandBuilder
				.minitransaction(bytes("<<ID>>"))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_1>>")))
				.withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays
								.asList(new Param(bytes("<<CHAVE_3>>")))))
				.build();

		dispatcher.dispatch(command);

		Mockito.verify(client).send(
				ref1,
				CommandBuilder
						.minitransaction(bytes("<<ID>>"))
						.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
						.withExtensionCommand(
								new ExtensionCommand(bytes("ABCD"),
										Arrays.asList(new Param(
												bytes("<<CHAVE_3>>")))))
						.build());
		Mockito.verify(client).send(
				ref2,
				CommandBuilder
						.minitransaction(bytes("<<ID>>"))
						.withWriteCommand(
								new WriteCommand(bytes("<<CHAVE_2>>"),
										bytes("<<DADOS_1>>"))).build());
	}

	@Test
	public void shouldDispatchAndCollectAccordingToMapper() throws Exception {

		InetSocketAddress addr1 = new InetSocketAddress(
				InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1235);

		InetSocketAddress addr2 = new InetSocketAddress(
				InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }), 1236);

		MemnodeReference ref1 = new MemnodeReference(addr1);
		MemnodeReference ref2 = new MemnodeReference(addr2);

		MemnodeClient client = Mockito.mock(MemnodeClient.class);

		MemnodeMapper mapper = Mockito.mock(MemnodeMapper.class);

		Mockito.when(mapper.map(bytes("<<CHAVE_1>>"))).thenReturn(ref1);
		Mockito.when(mapper.map(bytes("<<CHAVE_2>>"))).thenReturn(ref2);
		Mockito.when(mapper.map(bytes("<<CHAVE_3>>"))).thenReturn(ref1);
		Mockito.when(
				client.send(
						ref1,
						CommandBuilder
								.minitransaction(bytes("<<ID>>"))
								.withReadCommand(
										new ReadCommand(bytes("<<CHAVE_1>>")))
								.withExtensionCommand(
										new ExtensionCommand(bytes("ABCD"),
												Arrays.asList(new Param(
														bytes("<<CHAVE_3>>")))))
								.build())).thenReturn(
				CommandBuilder.minitransaction(bytes("<<ID>>"))
						.withCommitCommand().build());
		Mockito.when(
				client.send(
						ref2,
						CommandBuilder
								.minitransaction(bytes("<<ID>>"))
								.withWriteCommand(
										new WriteCommand(bytes("<<CHAVE_2>>"),
												bytes("<<DADOS_1>>"))).build()))
				.thenReturn(
						CommandBuilder
								.minitransaction(bytes("<<ID>>"))
								.withResultCommand(
										new ResultCommand(bytes("<<CHAVE_1>>"),
												bytes("<<DADOS_2")))
								.withCommitCommand().build());

		BasicMemnodeDispatcher dispatcher = new BasicMemnodeDispatcher(mapper, client);

		Command command = CommandBuilder
				.minitransaction(bytes("<<ID>>"))
				.withReadCommand(new ReadCommand(bytes("<<CHAVE_1>>")))
				.withWriteCommand(
						new WriteCommand(bytes("<<CHAVE_2>>"),
								bytes("<<DADOS_1>>")))
				.withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays
								.asList(new Param(bytes("<<CHAVE_3>>")))))
				.build();

		Command expected = CommandBuilder
				.minitransaction(bytes("<<ID>>"))
				.withResultCommand(
						new ResultCommand(bytes("<<CHAVE_1>>"),
								bytes("<<DADOS_2"))).withCommitCommand()
				.build();
		Command actual = dispatcher.dispatchAndCollect(command);

		Assert.assertEquals(expected, actual);
	}
}
