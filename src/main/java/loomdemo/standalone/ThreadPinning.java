/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package loomdemo.standalone;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shows concurrency behavior during {@code 10 Seconds} when using Virtual Threads that
 * use own (inner) mutex objects via {@code synchronized}. Due to kernel thread pinning we
 * expect a concurrency of {@link Runtime#availableProcessors()}.
 * <p>
 * This is an indication that the lock on the code path should be resolved into a
 * Loom-friendly lock mechanism.
 */
public class ThreadPinning {

	public static void main(String[] args) throws Exception {

		AtomicInteger concurrency = new AtomicInteger();

		for (int i = 0; i < 100; i++) {

			Thread.ofVirtual().start(() -> {

				try {
					Object mutex = new Object();
					synchronized (mutex) {
						concurrency.incrementAndGet();
						System.out.println("Switch to " + Thread.currentThread());
						Thread.sleep(1000);
						concurrency.decrementAndGet();
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}

		Instant waitUntil = Instant.now().plus(Duration.ofSeconds(10));

		while (waitUntil.isAfter(Instant.now())) {
			System.out.println("Concurrency: " + concurrency);
			Thread.sleep(100);
		}
	}

}
