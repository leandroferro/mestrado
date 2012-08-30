package br.usp.ime.protocol.parser;

import java.util.Arrays;

class Token {

	public static final Token EMPTY = new Token(TokenType.UNRECOGNIZED, new byte[0]);
	
	private final TokenType type;
	private final byte[] value;

	public Token(TokenType type, byte[] value) {
		this.type = type;
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (type != other.type)
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", value=" + Arrays.toString(value)
				+ ", strValue=" + new String(value) + "]";
	}

}
