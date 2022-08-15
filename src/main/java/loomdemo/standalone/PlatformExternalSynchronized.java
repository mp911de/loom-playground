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
 * Shows concurrency behavior during {@code 10 Seconds} when using Platform Threads that
 * use a shared mutex object via {@code synchronized}. Without further limits, we expect
 * {@code 116} active threads but with an effective concurrency of {@code 1}.
 */
public class PlatformExternalSynchronized {

	public static void main(String[] args) throws Exception {

		Object mutex = new Object();

		AtomicInteger concurrency = new AtomicInteger();
		AtomicInteger activeThreads = new AtomicInteger();

		synchronized (mutex) {

			for (int i = 0; i < 16; i++) {

				Thread.ofPlatform().start(() -> {

					try {
						activeThreads.incrementAndGet();
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
					activeThreads.decrementAndGet();
				});
			}

			Thread.sleep(100);

			for (int i = 0; i < 100; i++) {

				Thread.ofPlatform().start(() -> {

					try {
						activeThreads.incrementAndGet();
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

					activeThreads.decrementAndGet();
				});
			}
		}

		Instant waitUntil = Instant.now().plus(Duration.ofSeconds(10));

		while (waitUntil.isAfter(Instant.now())) {
			System.out.println("Concurrency: " + concurrency + ", Active Platform Threads: " + activeThreads);
			Thread.sleep(100);
		}
	}

}
