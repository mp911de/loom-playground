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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shows concurrency behavior during {@code 10 Seconds} when using Virtual Threads that
 * use a loom-friendly lock. We expect {@code 116} active virtual threads with an
 * effective concurrency of {@code 1}.
 */
public class VirtualExternalLocks {

	public static void main(String[] args) throws Exception {

		ReentrantLock lock = new ReentrantLock();

		AtomicInteger concurrency = new AtomicInteger();
		AtomicInteger activeThreads = new AtomicInteger();

		for (int i = 0; i < 16; i++) {

			Thread.ofVirtual().start(() -> {

				try {
					activeThreads.incrementAndGet();
					lock.lock();
					concurrency.incrementAndGet();
					System.out.println("Switch to " + Thread.currentThread());
					Thread.sleep(1000);
					concurrency.decrementAndGet();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				lock.unlock();
				activeThreads.decrementAndGet();
			});
		}

		Thread.sleep(100);

		for (int i = 0; i < 100; i++) {

			Thread.ofVirtual().start(() -> {

				try {
					activeThreads.incrementAndGet();
					lock.lock();
					concurrency.incrementAndGet();
					System.out.println("Switch to " + Thread.currentThread());
					Thread.sleep(1000);
					concurrency.decrementAndGet();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				lock.unlock();
				activeThreads.decrementAndGet();
			});
		}

		Instant waitUntil = Instant.now().plus(Duration.ofSeconds(10));

		while (waitUntil.isAfter(Instant.now())) {
			System.out.println("Concurrency: " + concurrency + ", Active Virtual Threads: " + activeThreads);
			Thread.sleep(100);
		}
	}

}
