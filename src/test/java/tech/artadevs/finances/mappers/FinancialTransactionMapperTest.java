package tech.artadevs.finances.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.artadevs.finances.dtos.FinancialTransactionResponseDto;
import tech.artadevs.finances.models.FinancialTransaction;

class FinancialTransactionMapperTest {

    private FinancialTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FinancialTransactionMapper();
    }

    @Test
    void shouldMapFinancialTransactionToResponseDto() {
        Date createdAt = new Date();
        Date updatedAt = new Date();

        FinancialTransaction transaction = new FinancialTransaction()
                .setId(1L)
                .setValue(new BigDecimal("100.00"))
                .setDescription("Test transaction")
                .setCreatedAt(createdAt)
                .setUpdatedAt(updatedAt);

        FinancialTransactionResponseDto response = mapper.toResponse(transaction);

        assertNotNull(response);
        assertEquals(transaction.getId(), response.getId());
        assertEquals(
                0,
                transaction.getValue().compareTo(response.getValue()));
        assertEquals(
                transaction.getDescription(),
                response.getDescription());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}