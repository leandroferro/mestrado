package br.usp.ime.memnode;

import static org.mockito.Mockito.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class SimpleProcessingScenario {

	private static final byte[] SOME_KEY = "chave-1".getBytes();
	private static final byte[] SOME_VALUE = "valor-1".getBytes(); 
	private static final byte[] ID = "123".getBytes();
	private Minitransaction minitransaction;
	private DataStore dataStore;
	private MinitransactionProcessor processor;

	public void setUp() {
		minitransaction = new Minitransaction(ID);
		
		dataStore = mock(DataStore.class);
		processor = new MinitransactionProcessor(dataStore);
		
		when(dataStore.read(SOME_KEY)).thenReturn(SOME_VALUE);
	}
	
	public void shouldReturnAnEmptyResult() {
		ProcessingResult expected = new OkProcessingResult(minitransaction);
		ProcessingResult actual = processor.process(minitransaction);
		
		Assert.assertEquals(expected, actual);
	}
	
	public void shouldReturnReadItems() {
		minitransaction.add(new ReadCommand(SOME_KEY));
		
		ProcessingResult expected = new OkProcessingResult(minitransaction, new ReadResultItem(SOME_KEY, SOME_VALUE));
		ProcessingResult actual = processor.process(minitransaction);
		
		Assert.assertEquals(expected, actual);
	}

}
