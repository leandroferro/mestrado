package br.usp.ime.memnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MemnodeReference {

	public static final String INVALID_REQUEST_RESPONSE = errorResponse("Invalid input request received");
	
	public static final String INVALID_COMMAND_RESPONSE = errorResponse("Invalid command request received");

	public static final String INVALID_ID_SIZE_RESPONSE = errorResponse("Invalid id size received");
	
	private static String errorResponse(String message) {
		return "P " + message.length() + " " + message;
	}
	
	private final Socket socket;

	public MemnodeReference(InetAddress byAddress, int port) throws IOException {
		socket = new Socket(byAddress, port);
	}
	
	private static boolean writeSilently(OutputStream output, byte[] data) {
		try {
			output.write(data);
			output.write('\n');
			output.flush();
			return true;
		} catch (IOException e) {
			LoggingService.logOut("S", e);
			return false;
		}
	}

	public ProcessingResult execute(Minitransaction minitransaction) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write("M".getBytes());
			outputStream.write(" ".getBytes());
			outputStream.write(Integer.toString(minitransaction.getId().length).getBytes());
			outputStream.write(" ".getBytes());
			outputStream.write(minitransaction.getId());
			outputStream.write(" ".getBytes());
			outputStream.write("{\n".getBytes());
			for(ReadCommand readCommand : minitransaction.getReadCommands()) {
				outputStream.write("L".getBytes());
				outputStream.write(" ".getBytes());
				outputStream.write(Integer.toString(readCommand.getKey().length).getBytes());
				outputStream.write(" ".getBytes());
				outputStream.write(readCommand.getKey());
				outputStream.write("\n".getBytes());
			}
			outputStream.write("}".getBytes());
			outputStream.flush();
			
			InputStream inputStream = socket.getInputStream();
			int charRead = inputStream.read();
			
			if( charRead == -1 || (char)charRead != 'M' ) {
				LoggingService.logOut("S", "Read " + (char)charRead);
				writeSilently(outputStream, INVALID_COMMAND_RESPONSE.getBytes());
			}
			else {
				charRead = inputStream.read();
				if( charRead == -1 || (char)charRead != ' ' ){
					LoggingService.logOut("S", "Read " + (char)charRead);
					writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
				}
				else {
					final StringBuilder builder = new StringBuilder();
					while( (charRead = inputStream.read()) > -1 && (char)charRead != ' ' ) {
						builder.append((char)charRead);
					}
					try {
						final int size = Integer.parseInt(builder.toString());
						
						builder.setLength(0);
						
						if( charRead == -1 || (char)charRead != ' ' ){
							LoggingService.logOut("S", "Read " + (char)charRead);
							writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
						}
						else {
							for(int count = 0; count < size && (charRead = inputStream.read()) > -1; count++ ) {
								builder.append((char)charRead);
							}
							
							if( size != builder.length() ) {
								LoggingService.logOut("S", "Read " + (char)charRead);
								writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());	
							}
							
							charRead = inputStream.read();
							if( charRead == -1 || (char)charRead != ' ' ){
								LoggingService.logOut("S", "Read " + (char)charRead);
								writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());	
							}
							else {
								charRead = inputStream.read();
								if( charRead == -1 || (char)charRead != '{' ){
									LoggingService.logOut("S", "Read " + (char)charRead);
									writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
								}
								else {
									charRead = inputStream.read();
									if( charRead == -1 || (char)charRead != '\n' ){
										LoggingService.logOut("S", "Read " + (char)charRead);
										writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
									}
									else {
										charRead = inputStream.read();
										if( charRead == -1 || (char)charRead != '}' ){
											LoggingService.logOut("S", "Read " + (char)charRead);
											writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
										}
										else {
											charRead = inputStream.read();
											if( charRead == -1 || (char)charRead != '\n' ){
												LoggingService.logOut("S", "Read " + (char)charRead);
												writeSilently(outputStream, INVALID_REQUEST_RESPONSE.getBytes());	
											}
											else {
												writeSilently(outputStream, ("M " + size + " " + builder.toString() + " {\n}").getBytes());
											}
										}
									}
								}
							}
						}
					} catch (NumberFormatException e) {
						writeSilently(outputStream, INVALID_ID_SIZE_RESPONSE.getBytes());
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new FailedProcessingResult(minitransaction, e.getMessage());
		}
	}

}
