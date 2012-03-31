package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import node.Controller;
import node.ExecutionResult;
import node.Minitransaction;

public class Server implements Runnable{

	private static final ExecutorService executorService = Executors.newFixedThreadPool(4); 
	private static final Logger logger = Logger.getLogger("Server");
	
	private final Controller controller;
	private final ServerSocket socket;
	private boolean shouldStop;
	
	private class ClientHandler implements Runnable{

		private final Socket client;
		
		private ClientHandler(Socket client) {
			this.client = client;
		}
		
		@Override
		public void run() {
			try {
				InputStreamMinitransactionFactory factory = new InputStreamMinitransactionFactory( client.getInputStream() );
				for(Minitransaction minitransaction = factory.create(); minitransaction != null; minitransaction = factory.create()) {
					logger.info("Vai executar " + minitransaction);
					ExecutionResult result = controller.execute( minitransaction );
					logger.info("Execucao resultou em " + result);
					SocketExecutionResultOutput output = new SocketExecutionResultOutput(client.getOutputStream());
					
					output.send(result);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if( client != null )
					try {
						client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		
	}

	public Server(Controller controller, ServerSocket socket) {
		super();
		this.controller = controller;
		this.socket = socket;
		
		shouldStop = false;
	}

	public void stop() {
		this.shouldStop = true;
	}
	
	@Override
	public void run() {
		logger.info("Esperando conexao");
		while( !shouldStop ) {
			logger.info("...");
			Socket client = null;
			try {
				client = socket.accept();
				
				logger.info("Aceitou conexao de " + client + " - vai mandar para outra thread e continuar ouvindo...");
				executorService.execute(new ClientHandler(client));
//				InputStreamMinitransactionFactory factory = new InputStreamMinitransactionFactory( client.getInputStream() );
//				for(Minitransaction minitransaction = factory.create(); minitransaction != null; minitransaction = factory.create()) {
//					logger.info("Vai executar " + minitransaction);
//					ExecutionResult result = controller.execute( minitransaction );
//					logger.info("Execucao resultou em " + result);
//					SocketExecutionResultOutput output = new SocketExecutionResultOutput(client.getOutputStream());
//					
//					output.send(result);
//				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
