package tech.artadevs.finances.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.mappers.BudgetMapper;
import tech.artadevs.finances.models.Budget;
import tech.artadevs.finances.models.User;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;
import tech.artadevs.finances.repositories.BudgetRepository;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Spy
    private BudgetMapper budgetMapper = new BudgetMapper();

    @InjectMocks
    private BudgetService budgetService;

    private User mockUser;
    private BudgetRequestDto request;
    private Budget budget;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        request = new BudgetRequestDto()
                .setName("Monthly food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusMonths(1))
                .setStatus(BudgetStatus.ACTIVE);

        budget = new Budget()
                .setId(1L)
                .setName("Monthly food budget")
                .setMonthlyLimit(new BigDecimal("1000.00"))
                .setCurrentSpending(new BigDecimal("250.00"))
                .setCategory(BudgetCategory.FOOD)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusMonths(1))
                .setStatus(BudgetStatus.ACTIVE)
                .setUser(mockUser);
    }

    @Test
    void shouldCreateBudget() {
        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponseDto response = budgetService.create(request);

        assertEquals("Monthly food budget", response.getName());
        assertEquals(new BigDecimal("1000.00"), response.getMonthlyLimit());
        assertEquals(new BigDecimal("250.00"), response.getCurrentSpending());
        assertEquals(new BigDecimal("750.00"), response.getRemainingBalance());
        assertEquals(BudgetStatus.ACTIVE, response.getStatus());

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void shouldMarkBudgetAsExceeded() {
        request.setCurrentSpending(new BigDecimal("1200.00"));

        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponseDto response = budgetService.create(request);

        assertEquals(BudgetStatus.EXCEEDED, response.getStatus());
        assertEquals(new BigDecimal("-200.00"), response.getRemainingBalance());
    }

    @Test
    void shouldListCurrentUserBudgets() {
        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.findAllByUserAndDeletedAtIsNull(mockUser))
                .thenReturn(List.of(budget));

        List<BudgetResponseDto> response = budgetService.listAllForCurrentUser();

        assertEquals(1, response.size());
        assertEquals("Monthly food budget", response.get(0).getName());
    }

    @Test
    void shouldGetOwnBudgetById() {
        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.findByIdAndUserAndDeletedAtIsNull(1L, mockUser))
                .thenReturn(Optional.of(budget));

        BudgetResponseDto response = budgetService.getOwnById(1L);

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("750.00"), response.getRemainingBalance());
    }

    @Test
    void shouldThrowNotFoundWhenBudgetDoesNotExist() {
        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.findByIdAndUserAndDeletedAtIsNull(1L, mockUser))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.getOwnById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldRejectInvalidDateRange() {
        request
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().minusDays(1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> budgetService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldUpdateBudget() {
        request
                .setName("Updated food budget")
                .setMonthlyLimit(new BigDecimal("1500.00"));

        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.findByIdAndUserAndDeletedAtIsNull(1L, mockUser))
                .thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponseDto response = budgetService.update(1L, request);

        assertEquals("Updated food budget", response.getName());
        assertEquals(new BigDecimal("1500.00"), response.getMonthlyLimit());
        assertEquals(new BigDecimal("1250.00"), response.getRemainingBalance());
    }

    @Test
    void shouldSoftDeleteBudget() {
        when(authenticationService.getCurrentUser()).thenReturn(mockUser);
        when(budgetRepository.findByIdAndUserAndDeletedAtIsNull(1L, mockUser))
                .thenReturn(Optional.of(budget));

        budgetService.deleteOwnById(1L);

        ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);

        verify(budgetRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDeletedAt());
    }
}