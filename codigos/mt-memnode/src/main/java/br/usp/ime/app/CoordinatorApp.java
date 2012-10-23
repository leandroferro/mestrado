package br.usp.ime.app;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import br.usp.ime.coordinator.BasicMemnodeClient;
import br.usp.ime.coordinator.BasicMemnodeDispatcher;
import br.usp.ime.coordinator.Coordinator;
import br.usp.ime.coordinator.DefaultCommandParserFactory;
import br.usp.ime.coordinator.MemnodeClient;
import br.usp.ime.coordinator.MemnodeDispatcher;
import br.usp.ime.coordinator.MemnodeReference;
import br.usp.ime.coordinator.SimpleMemnodeMapper;
import br.usp.ime.coordinator.SocketBasedConnectionProvider;
import br.usp.ime.coordinator.ThreadLocalConnectionFactory;
import br.usp.ime.protocol.parser.DefaultCommandSerializer;

public class CoordinatorApp {

	public static void main(String[] args) throws NumberFormatException, UnknownHostException {
		
		Options options = new Options();
		
		Option bindOption = new Option("b", "bind", true, "Address to bind coordinator");
		bindOption.setRequired(true);
		bindOption.setOptionalArg(false);
		bindOption.setArgName("bind address");
		bindOption.setArgs(1);
		
		Option memnodeOption = new Option("m", "memnode", true, "Address of memnode instance(s)");
		memnodeOption.setRequired(true);
		memnodeOption.setOptionalArg(false);
		memnodeOption.setArgName("memnode address");
		memnodeOption.setArgs(Option.UNLIMITED_VALUES);
		
		options.addOption(bindOption);
		options.addOption(memnodeOption);
		
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine commandLine = parser.parse(options, args, true);
			
			InetSocketAddress address = toInetSocketAddress(commandLine.getOptionValue(bindOption.getOpt()));
			
			SimpleMemnodeMapper memnodeMapper = new SimpleMemnodeMapper();
			List<String> memnodeAddresses = Arrays.asList( commandLine.getOptionValues(memnodeOption.getOpt()) );
			for (String addr : memnodeAddresses) {
				InetSocketAddress memnodeAddress = toInetSocketAddress(addr);
				memnodeMapper.add(new MemnodeReference(memnodeAddress));
			}
			
			MemnodeClient client = new BasicMemnodeClient(new DefaultCommandParserFactory(), new ThreadLocalConnectionFactory(new SocketBasedConnectionProvider()), DefaultCommandSerializer.instance);
			MemnodeDispatcher dispatcher = new BasicMemnodeDispatcher(memnodeMapper, client );
			Coordinator coordinator = new Coordinator(address, memnodeMapper, dispatcher, null);

			coordinator.start();
			
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( CoordinatorApp.class.getCanonicalName(), options );
		}
	}

	private static InetSocketAddress toInetSocketAddress(String bindAddress)
			throws UnknownHostException {
		String[] bindAddressArray = bindAddress.split(":");
		InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(bindAddressArray[0]), Integer.parseInt(bindAddressArray[1]));
		return address;
	}
	
}
