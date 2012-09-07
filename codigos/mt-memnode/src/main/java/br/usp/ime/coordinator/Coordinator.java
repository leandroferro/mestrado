package br.usp.ime.coordinator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.CommandSerializer;

public class Coordinator {

	private static final Logger logger = LoggerFactory.getLogger(Coordinator.class);
	
	private final SocketAddress address;

	private boolean shouldContinue;

	private int timeout = 500;

	private final MemnodeDispatcher dispatcher;

	public Coordinator(SocketAddress address, MemnodeDispatcher dispatcher) {
		this.address = address;
		this.dispatcher = dispatcher;
	}

	public void start() {
		try {
			
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.setSoTimeout(timeout );
			serverSocket.bind(address);
			logger.debug("Listening at {}", address);
			

			shouldContinue = true;
			while (shouldContinue) {
				try {
					logger.debug("Waiting connection");
					Socket client = serverSocket.accept();
					client.setTcpNoDelay(true);
					logger.debug("Connection stablished {}", client);
					
					InputStream inputStream = client.getInputStream();
					OutputStream outputStream = client.getOutputStream();

					CommandParser cmdParser = new CommandParser(inputStream);

					OutputStreamWriter writer = new OutputStreamWriter(outputStream);

					logger.debug("Waiting for command");
					
					Command command = cmdParser.parseNext();
					
					logger.debug("Command received: {}", command);
					
					if( command instanceof Minitransaction ) {
						Minitransaction minitransaction =  (Minitransaction)command;
						CommandBuilder finishOrAbortBuilder = CommandBuilder.minitransaction(minitransaction.getId());
						
						if( minitransaction.getReadCommands().size() > 0 ) {
							Command collect = dispatcher.dispatchAndCollect(minitransaction);
							CommandBuilder builder = CommandBuilder.minitransaction(minitransaction.getId());
							
							Minitransaction mCollect = (Minitransaction) collect;
							
							if( mCollect.getProblem() != null ) {
								builder = builder.withProblem(mCollect.getProblem());
								finishOrAbortBuilder = finishOrAbortBuilder.withAbortCommand();
							}
							else {
								for (ResultCommand r : mCollect.getResultCommands()) {
									builder = builder.withResultCommand(r);
								}
								
								builder = builder.withCommitCommand();
								finishOrAbortBuilder = finishOrAbortBuilder.withFinishCommand();
							}
							writer.append(CommandSerializer.serialize(builder.build()));
							dispatcher.dispatch(finishOrAbortBuilder.build());
						}
						else {
							writer.append(CommandSerializer.serialize(CommandBuilder.minitransaction(minitransaction.getId()).withCommitCommand().build()));
						}
					}
					else {
						writer.append(CommandSerializer.serialize(CommandBuilder.problem("Unknown command".getBytes()).build()));
					}
					writer.append("\n");
					writer.flush();

					
				} catch (SocketTimeoutException e) {
					logger.trace("Timeout waiting for connection");
				}
			}
			logger.debug("Exiting wait block");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		this.shouldContinue = false;
	}

}
