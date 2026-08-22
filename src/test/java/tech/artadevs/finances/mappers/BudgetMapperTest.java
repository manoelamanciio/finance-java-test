package tech.artadevs.finances.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.models.Budget;
import tech.artadevs.finances.models.User;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;

class BudgetMapperTest {

    private BudgetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BudgetMapper();
    }

    @Test
    void shouldMapRequestToEntity() {
        User user = new User().setId(1L);

        BudgetRequestDto request = new BudgetRequestDto()
                .setName("Food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.of(2026, 8, 1))
                .setEndDate(LocalDate.of(2026, 8, 31))
                .setStatus(BudgetStatus.ACTIVE);

        Budget budget = mapper.toEntity(request, user);

        assertNotNull(budget);
        assertEquals(request.getName(), budget.getName());
        assertEquals(
                0,
                request.getMonthlyLimit().compareTo(budget.getMonthlyLimit()));
        assertEquals(
                0,
                request.getCurrentSpending().compareTo(
                        budget.getCurrentSpending()));
        assertEquals(request.getCategory(), budget.getCategory());
        assertEquals(request.getStartDate(), budget.getStartDate());
        assertEquals(request.getEndDate(), budget.getEndDate());
        assertEquals(request.getStatus(), budget.getStatus());
        assertEquals(user, budget.getUser());
    }

    @Test
    void shouldMapEntityToResponse() {
        Date createdAt = new Date();
        Date updatedAt = new Date();

        Budget budget = new Budget()
                .setId(1L)
                .setName("Food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.of(2026, 8, 1))
                .setEndDate(LocalDate.of(2026, 8, 31))
                .setStatus(BudgetStatus.ACTIVE)
                .setCreatedAt(createdAt)
                .setUpdatedAt(updatedAt);

        BudgetResponseDto response = mapper.toResponse(budget);

        assertNotNull(response);
        assertEquals(budget.getId(), response.getId());
        assertEquals(budget.getName(), response.getName());
        assertEquals(
                0,
                budget.getMonthlyLimit().compareTo(
                        response.getMonthlyLimit()));
        assertEquals(
                0,
                budget.getCurrentSpending().compareTo(
                        response.getCurrentSpending()));
        assertEquals(
                0,
                new BigDecimal("750.00").compareTo(
                        response.getRemainingBalance()));
        assertEquals(budget.getCategory(), response.getCategory());
        assertEquals(budget.getStartDate(), response.getStartDate());
        assertEquals(budget.getEndDate(), response.getEndDate());
        assertEquals(budget.getStatus(), response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}