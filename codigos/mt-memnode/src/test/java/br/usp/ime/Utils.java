package br.usp.ime;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import br.usp.ime.memnode.ByteArrayWrapper;

public abstract class Utils {

	public static byte[] bytes(String str) {
		return str.getBytes();
	}

	public static InputStream stream(String str) {
		return new ByteArrayInputStream(bytes(str));
	}

	public static ByteArrayWrapper baw(String string) {
		return new ByteArrayWrapper(bytes(string));
	}
}
