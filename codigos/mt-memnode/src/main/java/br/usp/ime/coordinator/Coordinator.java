package br.usp.ime.coordinator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.AbortCommand;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.FinishCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.parser.DefaultCommandParser;
import br.usp.ime.protocol.parser.DefaultCommandSerializer;

public class Coordinator {

	private static final Logger logger = LoggerFactory
			.getLogger(Coordinator.class);

	private final InetSocketAddress address;

	private boolean shouldContinue;

	private int timeout = 500;

	private final MemnodeDispatcher dispatcher;

	private final MemnodeMapper mapper;

	public Coordinator(InetSocketAddress address, MemnodeMapper mapper, MemnodeDispatcher dispatcher) {
		this.address = address;
		this.mapper = mapper;
		this.dispatcher = dispatcher;
	}

	public void start() {
		try {

			ServerSocket serverSocket = new ServerSocket();
			serverSocket.setSoTimeout(timeout);
			serverSocket.bind(address);
			logger.debug("Listening at {}", address);

			shouldContinue = true;
			while (shouldContinue) {
				try {
					Socket client = serverSocket.accept();
					client.setTcpNoDelay(true);
					logger.debug("Connection stablished {}", client);

					InputStream inputStream = client.getInputStream();
					OutputStream outputStream = client.getOutputStream();

					DefaultCommandParser cmdParser = new DefaultCommandParser(
							inputStream);

					OutputStreamWriter writer = new OutputStreamWriter(
							outputStream);

					logger.debug("Waiting for command");

					for (Command command = cmdParser.parseNext(); shouldContinue
							&& command != null; command = cmdParser.parseNext()) {
						logger.info("Command received: {}", command);
						// Command command = cmdParser.parseNext();

						// logger.debug("Command received: {}", command);

						if (command instanceof Minitransaction) {
							Minitransaction minitransaction = (Minitransaction) command;

							if (minitransaction.hasActionCommands()) {
								
								// TODO vou precisar trazer o mapper pra ca...
								// Para o dispatcher a gente vai passar o mapeamento e o id da minitransacao...
								// Vou ter que guardar o id da minitransacao para poder fazer o dispatch do finish/abort
								
								MemnodeMapping mapping = mapper.map(minitransaction);
								
								MemnodeMapping collected = dispatcher.dispatchAndCollect(mapping);
								
//								Command collect = dispatcher
//										.dispatchAndCollect(minitransaction);
								
								logger.debug("Command collected: {}", collected);
								
								CommandBuilder builder = CommandBuilder
										.minitransaction(minitransaction
												.getId());

//								Minitransaction mCollect = (Minitransaction) collect;

								boolean finish = true;
								if (collected.hasProblem()) {
									Problem problem = collected
											.getProblem();
									logger.debug("Problem detected: {}", problem);
									builder = builder.withProblem(problem);
									finish = false;
								} else if (collected.hasNotCommitCommand()) {
									logger.debug("Not commit command received");
									builder = builder
											.withProblem(Problem.CANNOT_COMMIT);
									finish = false;
								} else {
									for (ResultCommand r : collected
											.getResultCommands()) {
										builder = builder.withResultCommand(r);
									}

									builder = builder.withCommitCommand();
								}
								Command returned = builder.build();
								logger.info("Returning {} to client", returned);
								writer.append(DefaultCommandSerializer
										.serializeCommand(returned));

								Command finishOrAbortCommand = finish ? FinishCommand.instance() : AbortCommand.instance();
								logger.info("Finishing minitransaction with {}", finishOrAbortCommand);
								MemnodeMapping replacedMapping = mapping.replaceCommands(finishOrAbortCommand);
								dispatcher.dispatch(replacedMapping);
							} else {
								logger.debug("Minitransaction doesn't have actions");
								writer.append(DefaultCommandSerializer
										.serializeCommand(CommandBuilder
												.minitransaction(
														minitransaction.getId())
												.withCommitCommand().build()));
							}
						} else {
							writer.append(DefaultCommandSerializer
									.serializeCommand(CommandBuilder.problem(
											"Unknown command".getBytes())
											.build()));
						}

						writer.append("\n");
						writer.flush();
					}

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

	@Override
	public String toString() {
		return "Coordinator [address=" + address + ", dispatcher=" + dispatcher
				+ "]";
	}

}
