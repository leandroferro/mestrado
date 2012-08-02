package br.usp.ime.memnode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientAndCoordinatorTest {

	private Coordinator coordinator;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private InetSocketAddress coordinatorAddress;
	private Executor executor = Executors.newFixedThreadPool(1);
	private ServerSocket serverSocket;

	@Before
	public void setUp() throws UnknownHostException, IOException {
		coordinatorAddress = new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 6969);
		
		serverSocket = new ServerSocket();
		serverSocket.bind(coordinatorAddress);
		coordinator = new Coordinator(serverSocket);
		
		executor.execute(new Runnable() {
			public void run() {
				coordinator.start();
			}
		});

		socket = new Socket();
		LoggingService.logErr("C", "Connecting with timeout=" + socket.getSoTimeout() + " and buffer size=" + socket.getSendBufferSize());
		socket.connect(coordinatorAddress);
		LoggingService.logErr("C", "Connected");
		writer = new PrintWriter(socket.getOutputStream(), true);
		reader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
	}

	@After
	public void tearDown() throws IOException {
		coordinator.stop();
		socket.close();
		serverSocket.close();
	}

	private static String join(String[] strings, String separator) {
		StringBuilder builder = new StringBuilder();
		for(String str : strings) {
			builder.append(str);
			builder.append(separator);
		}
		if(builder.length() >= separator.length())
			builder.setLength(builder.length() - separator.length());
		return builder.toString();
	}
	
	@Test
	public void shouldRefuseEmptyCommand() throws IOException {
		writer.format(" 1 2 {\n}\n");
		
		LoggingService.logErr("C", "Waiting response");
		Assert.assertEquals(Coordinator.INVALID_COMMAND_RESPONSE, reader.readLine());
	}
	
	@Test
	public void shouldRefuseBlankCommand() throws IOException {
		writer.format(" ");
		
		LoggingService.logErr("C", "Waiting response");
		Assert.assertEquals(Coordinator.INVALID_COMMAND_RESPONSE, reader.readLine());
	}

	@Test
	public void shouldRefuseUnknownCommand() throws IOException {
		writer.format("N 1 2 {\n}\n");
		
		LoggingService.logErr("C", "Waiting response");
		Assert.assertEquals(Coordinator.INVALID_COMMAND_RESPONSE, reader.readLine());
	}

	@Test
	public void shouldRefuseIncorrectIdSize() throws IOException {
		writer.format("M 2 2 {\n}\n");
		
		LoggingService.logErr("C", "Waiting response");
		Assert.assertEquals(Coordinator.INVALID_ID_SIZE_RESPONSE, reader.readLine());
	}
	
	@Test
	public void shouldRefuseInvalidIdSize() throws IOException {
		writer.format("M a 2 {\n}\n");
		
		LoggingService.logErr("C", "Waiting response");
		Assert.assertEquals(Coordinator.INVALID_ID_SIZE_RESPONSE, reader.readLine());
	}
	
	@Test
	public void shouldEchoEmptyMinitransaction() throws UnknownHostException,
			IOException {
		String minitransaction[] = new String[]{"M 3 1A3 {", "}"};
		LoggingService.logErr("C", join(minitransaction, "\n"));
		writer.format( join(minitransaction, "\n")+ "\n");

		Assert.assertEquals(minitransaction[0], reader.readLine());
		Assert.assertEquals(minitransaction[1], reader.readLine());
	}
	
	@Test
	public void shouldAcceptSpaceInsideId() throws UnknownHostException,
			IOException {
		String minitransaction[] = new String[]{"M 3 1 3 {", "}"};
		LoggingService.logErr("C", join(minitransaction, "\n"));
		writer.format( join(minitransaction, "\n")+ "\n");

		Assert.assertEquals(minitransaction[0], reader.readLine());
		Assert.assertEquals(minitransaction[1], reader.readLine());
	}

}
