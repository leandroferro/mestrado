package br.usp.ime.app;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import br.usp.ime.memnode.ByteArrayWrapper;
import br.usp.ime.memnode.DataStore;
import br.usp.ime.memnode.MapDataStore;
import br.usp.ime.memnode.Memnode;

public class MemnodeApp {

	public static void main(String[] args) throws NumberFormatException,
			UnknownHostException {

		Options options = new Options();

		Option bindOption = new Option("b", "bind", true,
				"Address to bind memnode");
		bindOption.setRequired(true);
		bindOption.setOptionalArg(false);
		bindOption.setArgName("bind address");
		bindOption.setArgs(1);

		options.addOption(bindOption);

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine commandLine = parser.parse(options, args, true);

			InetSocketAddress address = toInetSocketAddress(commandLine
					.getOptionValue(bindOption.getOpt()));

			Map<ByteArrayWrapper, ByteArrayWrapper> map = new HashMap<ByteArrayWrapper, ByteArrayWrapper>();
			DataStore dataStore = new MapDataStore(map);
			Memnode memnode = new Memnode(address, dataStore, null);

			memnode.start();

		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(MemnodeApp.class.getCanonicalName(), options);
		}
	}

	private static InetSocketAddress toInetSocketAddress(String bindAddress)
			throws UnknownHostException {
		String[] bindAddressArray = bindAddress.split(":");
		InetSocketAddress address = new InetSocketAddress(
				InetAddress.getByName(bindAddressArray[0]),
				Integer.parseInt(bindAddressArray[1]));
		return address;
	}

}
