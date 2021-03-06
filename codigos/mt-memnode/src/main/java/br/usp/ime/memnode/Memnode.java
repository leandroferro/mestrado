package br.usp.ime.memnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommandBuilder;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;
import br.usp.ime.protocol.parser.DefaultCommandParser;
import br.usp.ime.protocol.parser.DefaultCommandSerializer;

public class Memnode {

	private static final byte[] _newLine = "\n".getBytes();

	private static final Logger logger = LoggerFactory.getLogger(Memnode.class);

	private final SocketAddress address;

	private final DataStore dataStore;

	private final Map<ByteArrayWrapper, List<WriteCommand>> stageArea = new HashMap<ByteArrayWrapper, List<WriteCommand>>();

	private final Set<ByteArrayWrapper> cancelled = new HashSet<ByteArrayWrapper>();

	private boolean shouldContinue;

	private int timeout = 500;

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(4);

	private final LockManager lockManager;

	public Memnode(SocketAddress address, DataStore dataStore,
			LockManager lockManager) {
		this.address = address;
		this.dataStore = dataStore;
		this.lockManager = lockManager;
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

										boolean commit = true;
										boolean tryAgain = false;
										boolean abort = false;

										ByteArrayWrapper idWrapper = new ByteArrayWrapper(
												minitransaction.getId());

										if (cancelled.contains(idWrapper)) {
											logger.info(
													"Minitransaction {} is marked as cancelled",
													idWrapper);
											commit = false;
											abort = true;
										} else {

											if (minitransaction
													.getFinishCommand() != null) {
												if (stageArea
														.containsKey(idWrapper)) {

													logger.debug(
															"Received finish command and has staged commands to commit for {}",
															idWrapper);

													for (WriteCommand writeCommand : stageArea
															.get(idWrapper)) {
														try {
															logger.debug(
																	"Executing {}",
																	writeCommand);
															dataStore.write(
																	writeCommand
																			.getId(),
																	writeCommand
																			.getData());
														} catch (Exception e) {
															logger.error(
																	"Ops, an error occurred while commiting - aborting",
																	e);
															commit = false;
															break;
														}
													}

													lockManager
															.release(idWrapper);
													stageArea.remove(idWrapper);
													logger.debug(
															"Stage area cleaned {}",
															stageArea);
												} else {
													lockManager
															.release(idWrapper);
												}

											} else if (minitransaction
													.getAbortCommand() != null) {
												lockManager
														.release(new ByteArrayWrapper(
																minitransaction
																		.getId()));
												stageArea.remove(idWrapper);
												abort = true;
												cancelled.add(idWrapper);
											} else if (minitransaction
													.hasActionCommands()) {

												List<ByteArrayWrapper> readIds = new ArrayList<ByteArrayWrapper>();
												List<ByteArrayWrapper> writeIds = new ArrayList<ByteArrayWrapper>();

												for (ExtensionCommand extensionCommand : minitransaction
														.getExtensionCommands()) {
													readIds.add(new ByteArrayWrapper(
															extensionCommand
																	.getParams()
																	.get(0)
																	.getValue()));
												}
												for (ReadCommand readCommand : minitransaction
														.getReadCommands()) {
													readIds.add(new ByteArrayWrapper(
															readCommand
																	.getKey()));
												}
												for (WriteCommand writeCommand : minitransaction
														.getWriteCommands()) {
													writeIds.add(new ByteArrayWrapper(
															writeCommand
																	.getId()));
												}

												if (!lockManager.acquire(
														new ByteArrayWrapper(
																minitransaction
																		.getId()),
														readIds, writeIds)) {
													commit = false;
													tryAgain = true;
												} else {

													commit = executeExtensions(
															minitransaction);
												}

												if (commit) {
													for (ReadCommand readCommand : minitransaction
															.getReadCommands()) {
														logger.debug(
																"Executing {}",
																readCommand);
														logger.debug(
																"dataStore {}",
																dataStore);
														try {
															byte[] key = readCommand
																	.getKey();
															byte[] data = dataStore
																	.read(key);
															if (data != null) {
																builder = builder
																		.withResultCommand(new ResultCommand(
																				key,
																				data));
															}
														} catch (Exception e) {
															logger.error(
																	"Exception caught while reading data - aborting",
																	e);
															commit = false;
															break;
														}
													}
												}

												if (commit
														&& minitransaction
																.hasWriteCommands()) {
													stageArea
															.put(idWrapper,
																	minitransaction
																			.getWriteCommands());
													logger.debug(
															"Write commands staged {}",
															stageArea);
												}
											}
										}

										if (abort) {
											builder = builder
													.withAbortCommand();
										} else if (commit) {
											builder = builder
													.withCommitCommand();
										} else if (tryAgain) {
											builder = builder
													.withTryAgainCommand();
										} else {
											builder = builder
													.withProblem(new Problem(
															"ABORT".getBytes()));
										}

										Command returningCommand = builder
												.build();

										logger.debug("Returning {}",
												returningCommand);

										outputStream.write(DefaultCommandSerializer
												.serializeCommand(returningCommand).value);
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

									logger.debug("Waiting connection");
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					});

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
		return "Memnode [address=" + address + ", dataStore=" + dataStore
				+ ", stageArea=" + stageArea + "]";
	}

	private static Map<ByteArrayWrapper, Operation> extensionCommands = new HashMap<ByteArrayWrapper, Operation>(){
		{
			put(Operations.ECMP.identifier(), Operations.ECMP);
			put(Operations.NCMP.identifier(), Operations.NCMP);
		}
	};
	
	private boolean executeExtensions(Minitransaction minitransaction) {
		boolean commit = true;
		for (ExtensionCommand extensionCommand : minitransaction
				.getExtensionCommands()) {
			logger.debug(
					"Executing {}",
					extensionCommand);
			
			Operation operation = extensionCommands.get(new ByteArrayWrapper(extensionCommand.getId()));
			
			if( operation != null ) {
				try{
					commit = operation.execute(dataStore, extensionCommand);
				}
				catch (Exception e) {
					logger.error(
							"Exception caught while executing extension command - aborting",
							e);
					commit = false;
					break;
				}
			}
		}
		return commit;
	}

}
