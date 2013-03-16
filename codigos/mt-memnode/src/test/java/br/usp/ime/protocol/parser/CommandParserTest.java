package br.usp.ime.protocol.parser;

import static br.usp.ime.Utils.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.usp.ime.coordinator.SimpleIdGenerator;
import br.usp.ime.memnode.ByteArrayWrapper;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class CommandParserTest {

	@Test
	public void shouldReturnNullWhenEmptyStream()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream(""));
		Assert.assertNull(requestParser.parseNext());
	}
	
	@Test
	public void shouldParseEmptyMinitransaction()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 1 a {\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a")).build(),
				requestParser.parseNext());
	}
	
	@Test
	public void shouldParseMinitransactionWithCommitCommand()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 1 a {\nS\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a")).withCommitCommand().build(),
				requestParser.parseNext());
	}
	
	@Test
	public void shouldParseMinitransactionWithNotCommitCommand()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 1 a {\nN\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a")).withNotCommitCommand().build(),
				requestParser.parseNext());
	}
	
	@Test
	public void shouldParseMinitransactionWithFinishCommand()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 1 a {\nF\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a")).withFinishCommand().build(),
				requestParser.parseNext());
	}
	
	@Test
	public void shouldParseMinitransactionWithAbortCommand()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 1 a {\nA\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a")).withAbortCommand().build(),
				requestParser.parseNext());
	}

	@Test
	public void shouldParseMinitransactionWithSpaceInsideTheId()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("M 3 a b {\n}"));
		Assert.assertEquals(CommandBuilder.minitransaction(bytes("a b"))
				.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseProblem() throws UnexpectedTokenException {
		String hwhap = "Houston, we have a problem!";
		DefaultCommandParser requestParser = new DefaultCommandParser(stream("P "
				+ hwhap.length() + " " + hwhap));
		Assert.assertEquals(CommandBuilder.problem(bytes(hwhap)).build(),
				requestParser.parseNext());
	}

	@Test
	public void shouldParseProblemInsideMinitransaction()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 a {\nP 1 b\n}"));

		CommandBuilder builder = CommandBuilder.minitransaction(bytes("a"))
				.withProblem(new Problem(bytes("b")));

		Assert.assertEquals(builder.build(), requestParser.parseNext());
	}
	
	@Test
	public void shouldParseTryAgainCommand()
			throws UnexpectedTokenException {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 a {\nT\n}"));

		CommandBuilder builder = CommandBuilder.minitransaction(bytes("a"))
				.withTryAgainCommand();

		Assert.assertEquals(builder.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseTwoMinitransactionsInTheSameStream() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\n}\n   M 2 qw {\n\n}\n"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x")).build(),
				requestParser.parseNext());
		Assert.assertEquals(
				CommandBuilder.minitransaction(bytes("qw")).build(),
				requestParser.parseNext());
	}

	@Test
	public void shouldParseReadCommand() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nL 1 y\n}"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x"))
				.withReadCommand(new ReadCommand(bytes("y"))).build(),
				requestParser.parseNext());
	}

	@Test
	public void shouldParseReadCommands() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nL 1 y\nL 1 z\n}"));

		Assert.assertEquals(
				CommandBuilder.minitransaction(bytes("x"))
						.withReadCommand(new ReadCommand(bytes("y")))
						.withReadCommand(new ReadCommand(bytes("z"))).build(),
				requestParser.parseNext());
	}

	@Test
	public void shouldParseWriteCommand() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nE 1 y 2 zz\n}"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x"))
				.withWriteCommand(new WriteCommand(bytes("y"), bytes("zz")))
				.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseWriteCommands() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nE 1 y 2 zz\n E 2 zz 1 y}"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x"))
				.withWriteCommand(new WriteCommand(bytes("y"), bytes("zz")))
				.withWriteCommand(new WriteCommand(bytes("zz"), bytes("y")))
				.build(), requestParser.parseNext());
	}
	
	@Test
	public void shouldParseResultCommand() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nR 1 y 2 zz\n}"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x"))
				.withResultCommand(new ResultCommand(bytes("y"), bytes("zz")))
				.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseResultCommands() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nR 1 y 2 zz\n R 2 zz 1 y}"));

		Assert.assertEquals(CommandBuilder.minitransaction(bytes("x"))
				.withResultCommand(new ResultCommand(bytes("y"), bytes("zz")))
				.withResultCommand(new ResultCommand(bytes("zz"), bytes("y")))
				.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseExtensionCommand() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nC ABCD 1 y 2 zz 3 zzz\n}"));

		Assert.assertEquals(
				CommandBuilder
						.minitransaction(bytes("x"))
						.withExtensionCommand(
								new ExtensionCommand(bytes("ABCD"), Arrays
										.asList(new Param(bytes("y")),
												new Param(bytes("zz")),
												new Param(bytes("zzz")))))
						.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseExtensionCommands() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nC ABCD 1 y 2 zz 3 zzz\nC WXYZ 1 a\n}"));

		Assert.assertEquals(
				CommandBuilder
						.minitransaction(bytes("x"))
						.withExtensionCommand(
								new ExtensionCommand(bytes("ABCD"), Arrays
										.asList(new Param(bytes("y")),
												new Param(bytes("zz")),
												new Param(bytes("zzz")))))
						.withExtensionCommand(
								new ExtensionCommand(bytes("WXYZ"), Arrays
										.asList(new Param(bytes("a")))))
						.build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseCompleteMinitransaction() {
		DefaultCommandParser requestParser = new DefaultCommandParser(
				stream("M 1 x {\nC ABCD 1 y 2 zz 3 zzz\nC WXYZ 1 a\nL 3 abc\nE 5 xxxxx 3 olm\nL 2 ui}"));

		Assert.assertEquals(
				CommandBuilder
						.minitransaction(bytes("x"))
						.//
						withExtensionCommand(
								new ExtensionCommand(bytes("ABCD"), Arrays
										.asList(new Param(bytes("y")),
												new Param(bytes("zz")),
												new Param(bytes("zzz")))))
						.//
						withExtensionCommand(
								new ExtensionCommand(bytes("WXYZ"), Arrays
										.asList(new Param(bytes("a")))))
						.//
						withReadCommand(new ReadCommand(bytes("abc")))
						.//
						withReadCommand(new ReadCommand(bytes("ui")))
						.//
						withWriteCommand(
								new WriteCommand(bytes("xxxxx"), bytes("olm")))
						.//
						build(), requestParser.parseNext());
	}

	@Test
	public void shouldParseRealMinitransactionId() {
		
		SimpleIdGenerator generator = new SimpleIdGenerator();
		
		ByteArrayWrapper wrapper = generator.generate(baw("ABACABB11182934mkms"));
		
		List<Byte> bytes = new ArrayList<Byte>();
		for( byte b : ("M " + wrapper.value.length+ " ").getBytes() ){
			bytes.add(b);
		}
		for( byte b : wrapper.value ) {
			bytes.add(b);
		}
		for( byte b : (" {\n}").getBytes() ){
			bytes.add(b);
		}
		byte[] a = new byte[bytes.size()];
		for(int i = 0; i < bytes.size(); i++) {
			a[i] = bytes.get(i);
		}
		
		DefaultCommandParser requestParser = new DefaultCommandParser(
				new ByteArrayInputStream(a));
		
		Assert.assertEquals(
				CommandBuilder
						.minitransaction(wrapper.value).
						build(), requestParser.parseNext());
	}

	private ByteArrayWrapper baw(String string) {
		return new ByteArrayWrapper(string.getBytes());
	}
}
