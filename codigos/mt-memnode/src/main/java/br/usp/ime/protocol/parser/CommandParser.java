package br.usp.ime.protocol.parser;

import br.usp.ime.protocol.command.Command;

public interface CommandParser {

	Command parseNext() throws ParserException;

}
