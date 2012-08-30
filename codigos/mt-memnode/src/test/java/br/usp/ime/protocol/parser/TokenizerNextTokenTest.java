package br.usp.ime.protocol.parser;

import static br.usp.ime.Utils.*;
import static br.usp.ime.protocol.parser.TokenType.NUMBER;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TokenizerNextTokenTest {

	@Parameters
	public static Collection<Object[]> params() {
		return Arrays
				.asList( //
				new Object[] { "M  ",
						new Token(TokenType.MINITRANSACTION, bytes("M")) }, // 0
						new Object[] { "P",
								new Token(TokenType.PROBLEM, bytes("P")) }, //
						new Object[] { "L",
								new Token(TokenType.READ, bytes("L")) }, //
						new Object[] { "E",
								new Token(TokenType.WRITE, bytes("E")) }, //
						new Object[] {
								"C",
								new Token(TokenType.EXTENSION_COMMAND,
										bytes("C")) }, //
						new Object[] { "R",
								new Token(TokenType.RESULT, bytes("R")) }, // 5
						new Object[] { "S",
								new Token(TokenType.COMMIT, bytes("S")) }, //
						new Object[] { "N",
								new Token(TokenType.NOT_COMMIT, bytes("N")) }, //
						new Object[] { "F",
								new Token(TokenType.FINISH, bytes("F")) }, //
						new Object[] { "A",
								new Token(TokenType.ABORT, bytes("A")) }, //
						new Object[] { "123", new Token(NUMBER, bytes("123")) }, // 10
						new Object[] {
								"{",
								new Token(TokenType.OPENING_CURLY_BRACE,
										bytes("{")) }, // 
						new Object[] {
								"}",
								new Token(TokenType.CLOSING_CURLY_BRACE,
										bytes("}")) }, //
						new Object[] { "m",
								new Token(TokenType.UNRECOGNIZED, bytes("m")) }, //
						new Object[] {
								"njcnakjc  f4f 422 &V*S*SCC NS CS ",
								new Token(TokenType.UNRECOGNIZED,
										bytes("njcnakjc")) }, //
						new Object[] { "P1",
								new Token(TokenType.UNRECOGNIZED, bytes("P1")) }, //
						new Object[] { "", Token.EMPTY }, // 15
						new Object[] {
								" M",
								new Token(TokenType.MINITRANSACTION, bytes("M")) }, //
						new Object[] { "  P",
								new Token(TokenType.PROBLEM, bytes("P")) }, //
						new Object[] { "\tL",
								new Token(TokenType.READ, bytes("L")) }, //
						new Object[] { "\nE",
								new Token(TokenType.WRITE, bytes("E")) }, //
						new Object[] {
								" \n\tC",
								new Token(TokenType.EXTENSION_COMMAND,
										bytes("C")) }, //
						new Object[] { "   R",
								new Token(TokenType.RESULT, bytes("R")) }, // 21
						new Object[] { "\n \nN",
								new Token(TokenType.NOT_COMMIT, bytes("N")) }, //
						new Object[] { "\n\n\tF",
								new Token(TokenType.FINISH, bytes("F")) }, //
						new Object[] { " A",
								new Token(TokenType.ABORT, bytes("A")) } //
				);
	}

	private final String text;
	private final Token expected;

	public TokenizerNextTokenTest(String text, Token expected) {
		this.text = text;
		this.expected = expected;
	}

	@Test
	public void test() throws IOException {
		Tokenizer tokenizer = new Tokenizer(stream(text));

		Assert.assertEquals(expected, tokenizer.next());
	}

}
