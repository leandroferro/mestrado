package br.usp.ime.coordinator;

import br.usp.ime.memnode.ByteArrayWrapper;

public interface IdGenerator {

	ByteArrayWrapper generate(ByteArrayWrapper seed);

}
