package br.usp.ime.protocol.parser;

import static br.usp.ime.Utils.*;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TokenizerNextRawTest {

	@Test
	public void shouldReadSpecifiedBytesWhenAvailable() throws IOException, UnexpectedEndOfStream {
		String rawString = "m@# áºª\\|\tcdcd\n23dfr";
		String junkString = "aaaaaaaa";
		Tokenizer tokenizer = new Tokenizer(stream(rawString + junkString));
		
		Assert.assertEquals(new Token(TokenType.RAW, bytes(rawString)), tokenizer.nextRaw(rawString.getBytes().length));
	}
	
	@Test(expected=UnexpectedEndOfStream.class)
	public void shouldThrowExceptionWhenNotEnoughBytesAvailable() throws IOException, UnexpectedEndOfStream {
		String rawString = "m@# áºª\\|\tcdcd\n23dfr";
		Tokenizer tokenizer = new Tokenizer(stream(rawString));
		
		tokenizer.nextRaw(rawString.getBytes().length + 1);
	}
}
