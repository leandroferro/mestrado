package br.usp.ime.coordinator;

import static br.usp.ime.Utils.bytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.usp.ime.Utils;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.CommandSerializer;

public class BasicMemnodeClientTest {

	private static final InetSocketAddress ADDRESS;
	static {
		try {
			ADDRESS = new InetSocketAddress(InetAddress.getLocalHost(), 1928);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {

		MemnodeReference reference = new MemnodeReference(ADDRESS);
		Command toSend = CommandBuilder.minitransaction(bytes("<<TO_SEND>>"))
				.build();
		Command received = CommandBuilder
				.minitransaction(bytes("<<RECEIVED>>")).build();

		CommandParserFactory parserFactory = Mockito
				.mock(CommandParserFactory.class);
		CommandParser parser = Mockito.mock(CommandParser.class);
		CommandSerializer commandSerializer = Mockito
				.mock(CommandSerializer.class);
		Connection connection = Mockito.mock(Connection.class);
		ConnectionFactory connectionFactory = Mockito
				.mock(ConnectionFactory.class);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);

		Mockito.when(commandSerializer.serialize(toSend)).thenReturn(
				Utils.baw("<<SERIALIZED>>"));
		Mockito.when(connectionFactory.create(reference))
				.thenReturn(connection);
		Mockito.when(connection.getOutputStream()).thenReturn(outputStream);
		Mockito.when(connection.getInputStream()).thenReturn(inputStream);
		Mockito.when(parserFactory.createFor(inputStream)).thenReturn(parser);
		Mockito.when(parser.parseNext()).thenReturn(received);

		BasicMemnodeClient client = new BasicMemnodeClient(parserFactory,
				connectionFactory, commandSerializer);

		Command response = client.send(reference, toSend);

		Assert.assertArrayEquals("<<SERIALIZED>>\n".getBytes(),
				outputStream.toByteArray());
		Assert.assertEquals(received, response);
	}

}
