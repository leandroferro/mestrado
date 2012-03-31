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
		
		ServerSocket socket = new ServerSocket(Integer.parseInt(port), 1);
		
		Controller controller = new Controller();
		
		new Server(controller, socket).run();
		
	}
}
