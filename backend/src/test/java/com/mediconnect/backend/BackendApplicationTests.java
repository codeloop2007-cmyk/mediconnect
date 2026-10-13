package com.mediconnect.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration test requiring live PostgreSQL instance")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
