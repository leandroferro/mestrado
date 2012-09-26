package br.usp.ime.coordinator;

import br.usp.ime.protocol.command.Minitransaction;

public interface MemnodeMapper {

	MemnodeMapping map(Minitransaction minitransaction);

}
