package br.usp.ime.memnode;

import java.util.Arrays;

import br.usp.ime.protocol.command.ExtensionCommand;

public abstract class Operations {

	private static final ByteArrayWrapper _ECMP = new ByteArrayWrapper("ECMP".getBytes());
	protected static final Operation ECMP = new Operation() {

		@Override
		public ByteArrayWrapper identifier() {
			return _ECMP;
		}

		@Override
		public boolean execute(DataStore dataStore,
				ExtensionCommand extensionCommand) {
			byte[] data = dataStore
					.read(extensionCommand
							.getParams()
							.get(0)
							.getValue());
			return Arrays
					.equals(data,
							extensionCommand
							.getParams()
							.get(1)
							.getValue());
		}
		
	};
	protected static final ByteArrayWrapper _NCMP = new ByteArrayWrapper("NCMP".getBytes());
	protected static final Operation NCMP = new Operation() {

		@Override
		public ByteArrayWrapper identifier() {
			return _NCMP;
		}

		@Override
		public boolean execute(DataStore dataStore,
				ExtensionCommand extensionCommand) {
			return !ECMP.execute(dataStore, extensionCommand);
		}
		
	};

	private Operations() {
		
	}
}
