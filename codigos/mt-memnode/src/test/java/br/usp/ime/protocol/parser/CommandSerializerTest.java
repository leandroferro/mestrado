package br.usp.ime.protocol.parser;

import static br.usp.ime.Utils.*;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import br.usp.ime.Utils;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public class CommandSerializerTest {

	@Test
	public void shouldSerializeEmptyMinitransaction() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("abc"))
				.build();

		String expected = "M 3 abc {\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeProblem() {
		Command command = CommandBuilder.problem(Utils.bytes("abc")).build();

		String expected = "P 3 abc\n";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSerializeMinitransactionWithCommitCommand() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("abc")).withCommitCommand().build();

		String expected = "M 3 abc {\nS\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSerializeMinitransactionWithNotCommitCommand() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("abc")).withNotCommitCommand().build();

		String expected = "M 3 abc {\nN\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSerializeMinitransactionWithFinishCommand() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("abc")).withFinishCommand().build();

		String expected = "M 3 abc {\nF\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSerializeMinitransactionWithAbortCommand() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("abc")).withAbortCommand().build();

		String expected = "M 3 abc {\nA\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeMinitransactionWithProblem() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("xyz"))
				.withProblem(new Problem(Utils.bytes("abc"))).build();

		String expected = "M 3 xyz {\nP 3 abc\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeMinitransactionWithReadCommand() {
		Command command = CommandBuilder.minitransaction(Utils.bytes("xyz"))
				.withReadCommand(new ReadCommand(Utils.bytes("abc"))).build();

		String expected = "M 3 xyz {\nL 3 abc\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeMinitransactionWithWriteCommand() {
		Command command = CommandBuilder
				.minitransaction(Utils.bytes("xyz"))
				.withWriteCommand(
						new WriteCommand(Utils.bytes("abc"), Utils.bytes("def")))
				.build();

		String expected = "M 3 xyz {\nE 3 abc 3 def\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void shouldSerializeMinitransactionWithResultCommand() {
		Command command = CommandBuilder
				.minitransaction(Utils.bytes("xyz"))
				.withResultCommand(
						new ResultCommand(Utils.bytes("abc"), Utils.bytes("def")))
				.build();

		String expected = "M 3 xyz {\nR 3 abc 3 def\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeMinitransactionWithExtensionCommand() {
		Command command = CommandBuilder
				.minitransaction(Utils.bytes("xyz"))
				.withExtensionCommand(
						new ExtensionCommand(Utils.bytes("abc0"), Arrays
								.asList(new Param(Utils.bytes("def")),
										new Param(Utils.bytes("ghij")),
										new Param(Utils.bytes("klmno")))))
				.build();

		String expected = "M 3 xyz {\nC abc0 3 def 4 ghij 5 klmno\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldSerializeCompleteMinitransaction() {
		Command command = CommandBuilder
				.minitransaction(bytes("x"))
				.//
				withExtensionCommand(
						new ExtensionCommand(bytes("ABCD"), Arrays.asList(
								new Param(bytes("y")), new Param(bytes("zz")),
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
				withWriteCommand(new WriteCommand(bytes("xxxxx"), bytes("olm")))
				.//
				build();

		String expected = "M 1 x {\nC ABCD 1 y 2 zz 3 zzz\nC WXYZ 1 a\nL 3 abc\nL 2 ui\nE 5 xxxxx 3 olm\n}";
		String actual = CommandSerializer.serialize(command);

		Assert.assertEquals(expected, actual);
	}
}
