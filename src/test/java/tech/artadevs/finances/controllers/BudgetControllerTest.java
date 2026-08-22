package tech.artadevs.finances.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;
import tech.artadevs.finances.services.BudgetService;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private BudgetRequestDto request;
    private BudgetResponseDto response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(budgetController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        request = new BudgetRequestDto()
                .setName("Monthly food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusMonths(1))
                .setStatus(BudgetStatus.ACTIVE);

        response = new BudgetResponseDto()
                .setId(1L)
                .setName("Monthly food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setRemainingBalance(new BigDecimal("750.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusMonths(1))
                .setStatus(BudgetStatus.ACTIVE);
    }

    @Test
    void shouldCreateBudget() throws Exception {
        when(budgetService.create(any(BudgetRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/user/me/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly food budget"))
                .andExpect(jsonPath("$.monthlyLimit").value(1000.00))
                .andExpect(jsonPath("$.remainingBalance").value(750.00));
    }

    @Test
    void shouldListBudgets() throws Exception {
        when(budgetService.listAllForCurrentUser())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/user/me/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Monthly food budget"));
    }

    @Test
    void shouldGetBudgetById() throws Exception {
        when(budgetService.getOwnById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/user/me/budgets/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldUpdateBudget() throws Exception {
        response
                .setName("Updated budget")
                .setMonthlyLimit(new BigDecimal("1500.00"));

        when(budgetService.update(
                eq(1L),
                any(BudgetRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/user/me/budgets/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated budget"))
                .andExpect(jsonPath("$.monthlyLimit").value(1500.00));
    }

    @Test
    void shouldDeleteBudget() throws Exception {
        mockMvc.perform(delete("/user/me/budgets/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(budgetService).deleteOwnById(1L);
    }
}