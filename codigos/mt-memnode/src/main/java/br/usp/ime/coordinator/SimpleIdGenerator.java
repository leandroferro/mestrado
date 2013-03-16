package br.usp.ime.coordinator;

import java.util.concurrent.atomic.AtomicLong;

import br.usp.ime.memnode.ByteArrayWrapper;

import com.google.common.hash.Hashing;

public class SimpleIdGenerator implements IdGenerator {

	private AtomicLong counter = new AtomicLong(Long.MIN_VALUE);

	@Override
	public ByteArrayWrapper generate(ByteArrayWrapper seed) {

		return new ByteArrayWrapper(Hashing.sha256().newHasher().putString("mt")
				.putLong(counter.getAndIncrement()).putString(":").putBytes(seed.value).hash()
				.asBytes());

	}

}
