package br.usp.ime.protocol.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

/**
 * COMMAND :== MINITRANSACTION | PROBLEM
 * 
 * MINITRANSACTION :== 'M' NUMBER BSEQUENCE '{\n' (SUBCOMMANDS | PROBLEM |
 * RCOMMANDS | SCOMMAND | NCOMMAND | FCOMMAND | ACOMMAND ) '}'
 * 
 * PROBLEM :== 'P' NUMBER BSEQUENCE
 * 
 * SUBCOMMANDS :== ECOMMANDS | LCOMMANDS | CCOMMANDS | _
 * 
 * ECOMMANDS :== 'E' NUMBER BSEQUENCE NUMBER BSEQUENCE '\n' SUBCOMMANDS
 * 
 * LCOMMANDS :== 'L' NUMBER BSEQUENCE '\n' SUBCOMMANDS
 * 
 * CCOMMANDS :== 'C' NUMBER BSEQUENCE PSEQUENCE '\n' SUBCOMMANDS
 * 
 * PSEQUENCE :== NUMBER BSEQUENCE PSEQUENCE | _
 * 
 * RCOMMANDS :== 'R' NUMBER BSEQUENCE NUMBER BSEQUENCE RSEQUENCE '\n' RCOMMANDS
 * 
 * RSEQUENCE :== 'R' NUMBER BSEQUENCE NUMBER BSEQUENCE RSEQUENCE | _
 * 
 * SCOMMAND :== 'S' '\n'
 * 
 * NCOMMAND :== 'N' NUMBER BSEQUENCE '\n'
 * 
 * FCOMMAND :== 'F' '\n'
 * 
 * ACOMMAND :== 'A' '\n'
 * 
 * NUMBER :== a decimal number
 * 
 * BSEQUENCE :== a byte array
 * 
 * @author leandro
 * 
 */
public final class CommandParser {

	private static final Logger logger = LoggerFactory
			.getLogger(CommandParser.class);

	private final Tokenizer tokenizer;

	public CommandParser(InputStream inputStream) {
		this.tokenizer = new Tokenizer(inputStream);
	}

	private Token nextRawToken(int size) {
		try {
			logger.debug("Trying to read raw token with {}", size);
			final Token token = tokenizer.nextRaw(size);

			if (token.getType() != TokenType.RAW) {
				throw new ParserException("Expected sequence with " + size
						+ " but found " + token.getType());
			}

			__currentToken = null;

			return token;
		} catch (IOException e) {
			throw new ParserException(e);
		} catch (UnexpectedEndOfStream e) {
			throw new ParserException(e);
		}
	}

	private Token __currentToken = null;

	private Token nextToken() {
		try {
			if (__currentToken == null) {
				logger.debug("Trying to read next token");
				return __currentToken = tokenizer.next();
			} else {
				logger.debug("Returning cached token {}", __currentToken);
				return __currentToken;
			}
		} catch (IOException e) {
			throw new ParserException(e);
		}
	}

	private void advanceToken() {
		__currentToken = null;
	}

	private Token readAndCheck(EnumSet<TokenType> types) {
		final Token token = nextToken();

		if (!types.contains(token.getType())) {
			throw new ParserException("Expected " + types + " but found "
					+ token.getType());
		}

		advanceToken();

		return token;
	}

	public Command parseNext() throws ParserException {

		Token cmd = readAndCheck(EnumSet.of(TokenType.MINITRANSACTION,
				TokenType.PROBLEM, TokenType.UNRECOGNIZED));

		if (cmd == Token.EMPTY) {
			return null;
		}

		final Token param = loadParam();

		final CommandBuilder builder;

		if (cmd.getType() == TokenType.PROBLEM) {
			builder = CommandBuilder.problem(param.getValue());
		} else {

			parseMinitransaction(builder = CommandBuilder.minitransaction(param
					.getValue()));

		}

		return builder.build();
	}

	private Token loadParam() {
		final Token param = nextRawToken(Integer.parseInt(new String(
				readAndCheck(EnumSet.of(TokenType.NUMBER)).getValue())));

		return param;
	}

	private void parseMinitransaction(CommandBuilder builder) {
		readAndCheck(EnumSet.of(TokenType.OPENING_CURLY_BRACE));

		EnumSet<TokenType> types = EnumSet.of(TokenType.CLOSING_CURLY_BRACE,
				TokenType.PROBLEM, TokenType.READ, TokenType.WRITE,
				TokenType.EXTENSION_COMMAND, TokenType.COMMIT,
				TokenType.RESULT, TokenType.FINISH, TokenType.ABORT);

		for (Token subCommand = readAndCheck(types); //
		subCommand.getType() != TokenType.CLOSING_CURLY_BRACE; //
		subCommand = readAndCheck(types)) {

			if (subCommand.getType() == TokenType.EXTENSION_COMMAND) {
				Token extId = nextRawToken(4);

				Token nextToken = nextToken();

				List<Param> params = new ArrayList<Param>();

				while (nextToken.getType() == TokenType.NUMBER) {
					advanceToken();

					Token pToken = nextRawToken(Integer.parseInt(new String(
							nextToken.getValue())));

					params.add(new Param(pToken.getValue()));

					nextToken = nextToken();
				}

				builder.withExtensionCommand(new ExtensionCommand(extId
						.getValue(), params));
			} else if (subCommand.getType() == TokenType.COMMIT) {
				builder.withCommitCommand();
			} else if (subCommand.getType() == TokenType.FINISH) {
				builder.withFinishCommand();
			} else if (subCommand.getType() == TokenType.ABORT) {
				builder.withAbortCommand();
			} else {

				final Token param = loadParam();

				if (subCommand.getType() == TokenType.PROBLEM) {
					builder.withProblem(new Problem(param.getValue()));
				} else if (subCommand.getType() == TokenType.READ) {
					builder.withReadCommand(new ReadCommand(param.getValue()));
				} else if (subCommand.getType() == TokenType.WRITE) {
					builder.withWriteCommand(new WriteCommand(param.getValue(),
							loadParam().getValue()));
				} else if (subCommand.getType() == TokenType.RESULT) {
					builder.withResultCommand(new ResultCommand(param
							.getValue(), loadParam().getValue()));
				}
			}
		}

	}

}
