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
package benchmarks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author Mark Paluch
 */
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 2)
@Threads(8)
@Fork(value = 1, jvmArgs = {"--enable-preview", "-server", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms1024m", "-Xmx1024m",
		"-XX:MaxDirectMemorySize=1024m", "-noverify"})
@State(Scope.Benchmark)
@Testable
public class VirtualThreadsBenchmark {

	ThreadFactory virtualThreadFactory;
	ExecutorService executor;

	@Setup
	public void setUp() {
		virtualThreadFactory = Thread.ofVirtual().factory();
		executor = Executors.newVirtualThreadPerTaskExecutor();
	}

	@TearDown
	public void tearDown() {
		executor.close();
	}

	@Benchmark
	public void runAndAwait(Blackhole sink) throws ExecutionException, InterruptedException {
		sink.consume(executor.submit(() -> {
		}).get());
	}

	@Benchmark
	public void createVirtualThreads() {
		virtualThreadFactory.newThread(() -> {
		}).start();
	}
}
