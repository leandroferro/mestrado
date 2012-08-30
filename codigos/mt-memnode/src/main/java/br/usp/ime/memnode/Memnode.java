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
import br.usp.ime.protocol.parser.CommandParser;
import br.usp.ime.protocol.parser.CommandSerializer;

public class Memnode {

	private static final Logger logger = LoggerFactory.getLogger(Memnode.class);

	private final SocketAddress address;

	private final DataStore dataStore;

	private final Map<ByteArrayWrapper, List<WriteCommand>> stageArea = new HashMap<ByteArrayWrapper, List<WriteCommand>>();

	private boolean shouldContinue;

	private int timeout = 500;

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
			logger.debug("Waiting connection");
			while (shouldContinue) {
				try {
					Socket client = serverSocket.accept();
					client.setTcpNoDelay(true);
					logger.debug("Connection stablished {}", client);

					InputStream inputStream = client.getInputStream();
					OutputStream outputStream = client.getOutputStream();

					CommandParser cmdParser = new CommandParser(inputStream);

					OutputStreamWriter writer = new OutputStreamWriter(
							outputStream);

					logger.debug("Waiting for command");

					for (Command command = cmdParser.parseNext(); shouldContinue
							&& command != null; command = cmdParser.parseNext()) {
						logger.debug("Command received: {}", command);

						if (command instanceof Minitransaction) {
							Minitransaction minitransaction = (Minitransaction) command;

							CommandBuilder builder = CommandBuilder
									.minitransaction(minitransaction.getId());

							boolean commit = true;

							ByteArrayWrapper idWrapper = new ByteArrayWrapper(
									minitransaction.getId());
							
							if (minitransaction.getFinishCommand() != null
									&& stageArea
											.containsKey(idWrapper)) {
								for (WriteCommand writeCommand : stageArea.get(idWrapper)) {
									try {
										dataStore.write(writeCommand.getId(),
												writeCommand.getData());
									} catch (Exception e) {
										commit = false;
										break;
									}
								}
								stageArea.remove(idWrapper);
							} else {
								for (ExtensionCommand extensionCommand : minitransaction
										.getExtensionCommands()) {
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
											commit = false;
											break;
										}
									}
								}

								if (commit) {
									for (ReadCommand readCommand : minitransaction
											.getReadCommands()) {
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
											commit = false;
											break;
										}
									}
								}

								if (commit) {
									stageArea.put(idWrapper,
											minitransaction.getWriteCommands());
								}
							}

							if (commit) {
								builder = builder.withCommitCommand();
							} else {
								builder = builder.withProblem(new Problem(
										"ABORT".getBytes()));
							}

							writer.append(CommandSerializer.serialize(builder
									.build()));
						} else {
							writer.append(CommandSerializer
									.serialize(CommandBuilder.problem(
											"Unknown command".getBytes())
											.build()));
						}
						writer.append("\n");
						writer.flush();
						
						logger.debug("Waiting connection");
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

	public static void main(String[] args) throws UnknownHostException {
		Memnode memnode = new Memnode(new InetSocketAddress(InetAddress.getLocalHost(), 6060), new MapDataStore());
		
		memnode.start();
	}
}
