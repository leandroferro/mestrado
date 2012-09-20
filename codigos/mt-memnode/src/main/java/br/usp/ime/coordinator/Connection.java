package br.usp.ime.coordinator;

import java.io.InputStream;
import java.io.OutputStream;

public interface Connection {

	OutputStream getOutputStream();

	InputStream getInputStream();

}
