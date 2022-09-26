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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MXBean;

/**
 * Shows concurrency behavior during {@code 10 Seconds} when using Virtual Threads that
 * use internal loom-friendly locks. We expect {@code 1000} active virtual threads with an
 * effective concurrency of {@code 1000}.
 * <p>
 * The CPU and memory profile is much more streamlined in comparison to platform threads.
 */
public class VirtualInternalLocks {

	public static void main(String[] args) throws Exception {

		AtomicInteger concurrency = new AtomicInteger();
		AtomicInteger activeThreads = new AtomicInteger();

		for (int i = 0; i < 1000; i++) {

			Thread.ofVirtual().start(() -> {

				ReentrantLock lock = new ReentrantLock();
				try {
					activeThreads.incrementAndGet();
					lock.lock();
					concurrency.incrementAndGet();
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

		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = memBean.getHeapMemoryUsage();
		MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

		while (waitUntil.isAfter(Instant.now())) {

			System.out.println("Concurrency: %4s, Active Virtual Threads: %4s, heap: %,d kB, non-heap: %,d kB"
					.formatted(concurrency, activeThreads, heap.getUsed() / 1000, nonHeap.getUsed() / 1000));
			Thread.sleep(100);
		}
	}

}
