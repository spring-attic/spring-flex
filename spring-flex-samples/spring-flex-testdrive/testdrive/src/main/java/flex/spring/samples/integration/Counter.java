package flex.spring.samples.integration;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

	private final AtomicInteger count = new AtomicInteger();

	public int next() {
		return count.incrementAndGet();
	}

}
