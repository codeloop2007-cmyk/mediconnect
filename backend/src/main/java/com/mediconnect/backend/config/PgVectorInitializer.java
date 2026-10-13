package com.mediconnect.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PgVectorInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PgVectorInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public PgVectorInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Enable vector extension if not present
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector;");
            
            // Add 384-dimensional vector column to doctors table
            jdbcTemplate.execute("ALTER TABLE doctors ADD COLUMN IF NOT EXISTS embedding vector(384);");
            
            logger.info("Successfully verified PostgreSQL pgvector extension and doctors.embedding column.");
        } catch (Exception e) {
            logger.warn("Notice: pgvector column initialization skipped or encountered error: {}", e.getMessage());
        }
    }
}

