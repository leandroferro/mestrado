package app;

import java.io.IOException;
import java.net.ServerSocket;

import net.Server;
import node.Controller;

public class Main {

	private static String getOr(String[] args, int idx, String defaultValue) {
		if( idx < args.length )
			return args[idx];
		else
			return defaultValue;
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		
		String port = getOr(args, 0, "8069");
		
		ServerSocket socket = new ServerSocket(Integer.parseInt(port));
		
		Controller controller = new Controller();
		
		Server server = new Server(controller, socket);
		
		Thread thread = new Thread(server);
		thread.start();
		
		System.out.println("Pressione qualquer tecla para terminar...");
		System.in.read();
		
		server.stop();
		thread.join();
		
	}
}
