package br.usp.ime.memnode;

public class MinitransactionProcessor {

	private final DataStore dataStore;

	public MinitransactionProcessor(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public ProcessingResult process(Minitransaction minitransaction){
//		dataStore.read(minitransaction)
		return new OkProcessingResult(minitransaction);
	}
	
}
