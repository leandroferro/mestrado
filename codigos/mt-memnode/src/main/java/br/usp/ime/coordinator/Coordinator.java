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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.memnode.ByteArrayWrapper;
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

	private static final byte[] _newLine = "\n".getBytes();

	private static final Logger logger = LoggerFactory
			.getLogger(Coordinator.class);

	private final InetSocketAddress address;

	private boolean shouldContinue;

	private int timeout = 500;

	private final MemnodeDispatcher dispatcher;

	private final MemnodeMapper mapper;

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(4);

	public Coordinator(InetSocketAddress address, MemnodeMapper mapper,
			MemnodeDispatcher dispatcher) {
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

					final Socket client = serverSocket.accept();
					client.setTcpNoDelay(true);
					logger.debug(
							"Connection stablished {} - dispatching to handler",
							client);

					executorService.execute(new Runnable() {

						@Override
						public void run() {
							try {

								InputStream inputStream = client
										.getInputStream();
								OutputStream outputStream = client
										.getOutputStream();

								DefaultCommandParser cmdParser = new DefaultCommandParser(
										inputStream);

								logger.debug("Waiting for command");

								for (Command command = cmdParser.parseNext(); shouldContinue
										&& command != null; command = cmdParser
										.parseNext()) {
									logger.info("Command received: {}", command);

									if (command instanceof Minitransaction) {
										Minitransaction minitransaction = (Minitransaction) command;
										
										CommandBuilder builder = CommandBuilder
												.minitransaction(minitransaction
														.getId());

										if (minitransaction.hasActionCommands()) {
											boolean repeat = false;
											MemnodeMapping mapping = mapper
													.map(minitransaction);
											logger.debug("Mapped {}", mapping);
											do {
												MemnodeMapping collected = dispatcher
														.dispatchAndCollect(mapping);

												logger.debug(
														"Command collected: {}",
														collected);

												boolean finish = true;
												if (collected.hasProblem()) {
													Problem problem = collected
															.getProblem();
													logger.debug(
															"Problem detected: {}",
															problem);
													builder = builder
															.withProblem(problem);
													finish = false;
													repeat = false;
												} else if (collected
														.hasNotCommitCommand()) {
													logger.debug("Not commit command received");
													builder = builder
															.withProblem(Problem.CANNOT_COMMIT);
													finish = false;
													repeat = false;
												} else if (collected.hasTryAgainCommand()) {
													logger.debug("Try again command received");
													finish = false;
													repeat = true;
												} else {
													for (ResultCommand r : collected
															.getResultCommands()) {
														builder = builder.withResultCommand(r);
													}
													builder = builder.withCommitCommand();
													finish = true;
													repeat = false;
												}
												

												Command finishOrAbortCommand = finish ? FinishCommand
														.instance()
														: AbortCommand
																.instance();
												logger.info(
														"Finishing minitransaction with {}",
														finishOrAbortCommand);
												MemnodeMapping replacedMapping = mapping
														.replaceCommands(finishOrAbortCommand);
												dispatcher
														.dispatch(replacedMapping);
											} while (repeat);
											
											Command returned = builder
													.build();
											logger.info(
													"Returning {} to client",
													returned);
											outputStream.write(DefaultCommandSerializer
													.serializeCommand(returned).value);
										} else {
											logger.debug("Minitransaction doesn't have actions");
											outputStream.write(DefaultCommandSerializer
													.serializeCommand(CommandBuilder
															.minitransaction(
																	minitransaction
																			.getId())
															.withCommitCommand()
															.build()).value);
										}
									} else {
										outputStream.write(DefaultCommandSerializer
												.serializeCommand(CommandBuilder
														.problem(
																"Unknown command"
																		.getBytes())
														.build()).value);
									}

									outputStream.write(_newLine);
									outputStream.flush();
								}

								client.close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}

					});

				} catch (SocketTimeoutException e) {
					logger.trace("Timeout waiting for connection");
				}
			}
			logger.debug("Leaving wait block");
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
