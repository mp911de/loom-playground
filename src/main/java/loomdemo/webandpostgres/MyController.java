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
package loomdemo.webandpostgres;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller measuring concurrency.
 *
 * @author Mark Paluch
 */
@RestController
public class MyController {

	private static AtomicLong concurrency = new AtomicLong();

	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;

	public MyController(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	@GetMapping("/sleep/{amount}")
	public String sleep(@PathVariable long amount) {

		String concurrency = "" + MyController.concurrency.incrementAndGet();
		try {
			return "Concurrency: " + concurrency + ", Result: " + jdbcTemplate.queryForMap("SELECT clock_timestamp(), pg_sleep(" + amount + ")");
		}
		finally {
			MyController.concurrency.decrementAndGet();
		}
	}

	@GetMapping("/transactional/{amount}")
	public String extendedSleep(@PathVariable long amount) {
		return transactionTemplate.execute(status -> sleep(amount));
	}
}
