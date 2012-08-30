package br.usp.ime;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

public abstract class Utils {

	public static byte[] bytes(String str) {
		return str.getBytes();
	}

	public static InputStream stream(String str) {
		return new ByteArrayInputStream(bytes(str));
	}
}
