package br.usp.ime.protocol.parser;

@SuppressWarnings("serial")
public class UnexpectedTokenException extends Exception {

	public UnexpectedTokenException(final Token token) {
		super("Unexpected token: " + token);
	}

	public UnexpectedTokenException(String msg) {
		super(msg);
	}

}
