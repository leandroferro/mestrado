package br.usp.ime.protocol.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tokenizer {

	private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);
	private final InputStream stream;
	private final byte[] buffer = new byte[50];
	private int readBytes;

	public Tokenizer(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * Not thread-safe
	 * 
	 * @return
	 * @throws IOException
	 */
	public Token next() throws IOException {

		readUntil();
		Token token = token();
		logger.debug("Read {}", token);
		return token;
	}

	private Token token() {
		if (readBytes == 0)
			return Token.EMPTY;
		return new Token(type(), buffer());
	}

	private TokenType type() {
		if (readBytes == 1 && isReserved(buffer[0])) {
			return type(buffer[0]);
		}

		boolean isNumber = true;
		for (int j = 0; j < readBytes && (isNumber &= isNumber(buffer[j])); j++) {

		}
		return isNumber ? TokenType.NUMBER : TokenType.UNRECOGNIZED;
	}

	private TokenType type(byte b) {
		return reserveds.get(b);
	}

	private byte[] buffer() {
		return Arrays.copyOf(buffer, readBytes);
	}

	private void readUntil() throws IOException {
		readBytes = 0;
		
		int b = -1;
		while ((b = stream.read()) > -1) {
			if (!Character.isWhitespace(b)) {
				break;
			}
		}

		if (b > -1) {
			buffer[readBytes++] = (byte) b;
			for (; readBytes < buffer.length
					&& stream.read(buffer, readBytes, 1) > 0
					&& !Character.isWhitespace(buffer[readBytes]); readBytes++) {
			}
		}
	}

	private static boolean isNumber(byte b) {
		return b >= '0' && b <= '9';
	}

	@SuppressWarnings("serial")
	private static final Map<Byte, TokenType> reserveds = new HashMap<Byte, TokenType>() {
		{
			put((byte) 'P', TokenType.PROBLEM);
			put((byte) 'M', TokenType.MINITRANSACTION);
			put((byte) 'L', TokenType.READ);
			put((byte) 'E', TokenType.WRITE);
			put((byte) 'C', TokenType.EXTENSION_COMMAND);
			put((byte) 'R', TokenType.RESULT);
			put((byte) 'S', TokenType.COMMIT);
			put((byte) 'N', TokenType.NOT_COMMIT);
			put((byte) 'F', TokenType.FINISH);
			put((byte) 'A', TokenType.ABORT);
			put((byte) '{', TokenType.OPENING_CURLY_BRACE);
			put((byte) '}', TokenType.CLOSING_CURLY_BRACE);
			put((byte) '\n', TokenType.NEW_LINE);
		}
	};

	private static boolean isReserved(byte b) {
		return reserveds.keySet().contains(b);
	}

	public Token nextRaw(int length) throws IOException, UnexpectedEndOfStream {
		byte[] buffer = new byte[length];
		readBytes = stream.read(buffer, 0, length);

		if (readBytes < length) {
			throw new UnexpectedEndOfStream();
		}

		Token token = new Token(TokenType.RAW, Arrays.copyOf(buffer, length));
		
		logger.debug("Read {}", token);
		return token;
	}

}
