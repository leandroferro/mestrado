package net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import server.ReadResult;

import node.ExecutionResult;

public class SocketExecutionResultOutput {

	private final Writer writer;
	
	public SocketExecutionResultOutput(OutputStream outputStream) {
		this.writer = new OutputStreamWriter(outputStream);
	}

	public boolean send(ExecutionResult result) {
		try {
			writer.write(result.getOutcome() + " " + result.getId());
			writer.write('\n');
			Iterator<ReadResult> iterator = result.getReadResultIterator();
			while(iterator.hasNext()) {
				ReadResult readResult = iterator.next();
				writer.write("READ " + readResult.getAddress() + " " + new String(readResult.getData()));
				writer.write('\n');
			}
			writer.write("END");
			writer.write('\n');
			writer.flush();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
}
