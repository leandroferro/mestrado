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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final Logger logger = LoggerFactory.getLogger(Memnode.class);

	private final SocketAddress address;

	private final DataStore dataStore;

	private final Map<ByteArrayWrapper, List<WriteCommand>> stageArea = new HashMap<ByteArrayWrapper, List<WriteCommand>>();

	private boolean shouldContinue;

	private int timeout = 500;
	
	private final ExecutorService executorService = Executors
			.newFixedThreadPool(4);

	public Memnode(SocketAddress address, DataStore dataStore) {
		this.address = address;
		this.dataStore = dataStore;
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
					logger.debug("Connection stablished {} - dispatching to handler", client);

					executorService.execute(new Runnable() {

						@Override
						public void run() {
							try{
								
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
									
									if (command instanceof Minitransaction) {
										Minitransaction minitransaction = (Minitransaction) command;
										
										CommandBuilder builder = CommandBuilder
												.minitransaction(minitransaction.getId());
										
										boolean commit = true;
										
										ByteArrayWrapper idWrapper = new ByteArrayWrapper(
												minitransaction.getId());
										
										if (minitransaction.getFinishCommand() != null
												&& stageArea.containsKey(idWrapper)) {
											
											logger.debug("Received finish command and has staged commands to commit for {}", idWrapper);
											
											for (WriteCommand writeCommand : stageArea
													.get(idWrapper)) {
												try {
													logger.debug("Executing {}", writeCommand);
													dataStore.write(writeCommand.getId(),
															writeCommand.getData());
												} catch (Exception e) {
													logger.error("Ops, an error occurred while commiting - aborting", e);
													commit = false;
													break;
												}
											}
											stageArea.remove(idWrapper);
											logger.debug("Stage area cleaned {}", stageArea);
										} else {
											for (ExtensionCommand extensionCommand : minitransaction
													.getExtensionCommands()) {
												logger.debug("Executing {}", extensionCommand);
												if ("ECMP".equals(new String(
														extensionCommand.getId()))) {
													try {
														byte[] data = dataStore
																.read(extensionCommand
																		.getParams().get(0)
																		.getValue());
														if (!Arrays.equals(data,
																extensionCommand
																.getParams().get(1)
																.getValue())) {
															commit = false;
														}
													} catch (Exception e) {
														logger.error("Exception caught while executing extension command - aborting", e);
														commit = false;
														break;
													}
												}
											}
											
											if (commit) {
												for (ReadCommand readCommand : minitransaction
														.getReadCommands()) {
													logger.debug("Executing {}", readCommand);
													try {
														byte[] data = dataStore
																.read(readCommand.getKey());
														if (data != null) {
															builder = builder
																	.withResultCommand(new ResultCommand(
																			readCommand
																			.getKey(),
																			data));
														}
													} catch (Exception e) {
														logger.error("Exception caught while reading data - aborting", e);
														commit = false;
														break;
													}
												}
											}
											
											if (commit && minitransaction.hasWriteCommands()) {
												stageArea.put(idWrapper,
														minitransaction.getWriteCommands());
												logger.debug("Write commands staged {}", stageArea);
											}
										}
										
										if (commit) {
											builder = builder.withCommitCommand();
										} else {
											builder = builder.withProblem(new Problem(
													"ABORT".getBytes()));
										}
										
										Command returningCommand = builder.build();
										
										logger.debug("Returning {}", returningCommand);
										
										writer.append(DefaultCommandSerializer
												.serializeCommand(returningCommand));
									} else {
										writer.append(DefaultCommandSerializer
												.serializeCommand(CommandBuilder.problem(
														"Unknown command".getBytes())
														.build()));
									}
									writer.append("\n");
									writer.flush();
									
									logger.debug("Waiting connection");
								}
							}
							catch(IOException e) {
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

}
