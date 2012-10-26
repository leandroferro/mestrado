package br.usp.ime.memnode;

import static java.util.Arrays.asList;

import org.junit.Assert;
import org.junit.Test;

public class SimpleLockManagerTest {

	private static ByteArrayWrapper baw(String s) {
		return new ByteArrayWrapper(s.getBytes());
	}

	@Test
	public void test() {
		SimpleLockManager manager = new SimpleLockManager();

		Assert.assertTrue(manager.acquire(baw("M_1"),
				asList(baw("CL_1"), baw("CL_2")),
				asList(baw("CE_1"), baw("CE_2"))));
		
		Assert.assertTrue(manager.acquire(baw("M_2"),
				asList(baw("CL_1"), baw("CL_2"), baw("CL_3"), baw("CL_4")),
				asList(baw("CE_3"), baw("CE_4"))));
		
		// nao consegue por que CL_1 esta lockado para leitura por M_1 e M_2
		Assert.assertFalse(manager.acquire(baw("M_3"),
				asList(baw("CL_5"), baw("CL_6")),
				asList(baw("CL_1"), baw("CE_5"), baw("CE_6"))));
		
		// nao consegue por que CE_1 esta lockado para escrita por M_1
		Assert.assertFalse(manager.acquire(baw("M_4"),
				asList(baw("CE_1")),
				asList(baw("CE_7"))));
		
		manager.release(baw("M_1"));
		
		Assert.assertTrue(manager.acquire(baw("M_4"),
				asList(baw("CE_1")),
				asList(baw("CE_7"))));

		// ainda nao consegue por que so liberei a travado em CL_1 de M_1, ainda falta de M_2
		Assert.assertFalse(manager.acquire(baw("M_3"),
				asList(baw("CL_5"), baw("CL_6")),
				asList(baw("CL_1"), baw("CE_5"), baw("CE_6"))));
		
		manager.release(baw("M_2"));
		
		Assert.assertTrue(manager.acquire(baw("M_3"),
				asList(baw("CL_5"), baw("CL_6")),
				asList(baw("CL_1"), baw("CE_5"), baw("CE_6"))));
		
	}

}
