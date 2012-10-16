package br.usp.ime.protocol;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class ConsistentHashTest {

	@Test
	public void test() {
		HashFunction hashFunction = Hashing.md5();
		int v[] = {0,0};
		int j = 1000000;
		for(int i = 0; i < j; i++) {
			HashCode hashCode = hashFunction.newHasher().putInt(i).hash();
			int consistentHash = Hashing.consistentHash(hashCode, v.length);
//			System.out.printf("CH of %d (%s) is %d%n", i, hashCode, consistentHash);
			v[consistentHash]++;
		}
		for(int i=0;i<v.length;i++) {
			System.out.printf("v[%d]=%d (%f)%n", i, v[i], v[i]/(j*1.0));
		}
	}

}
