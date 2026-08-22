package tech.artadevs.finances.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.mappers.BudgetMapper;
import tech.artadevs.finances.models.Budget;
import tech.artadevs.finances.models.User;
import tech.artadevs.finances.models.enums.BudgetStatus;
import tech.artadevs.finances.repositories.BudgetRepository;

@Service
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AuthenticationService authenticationService;
    private final BudgetMapper budgetMapper;

    public BudgetService(
            BudgetRepository budgetRepository,
            AuthenticationService authenticationService,
            BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.authenticationService = authenticationService;
        this.budgetMapper = budgetMapper;
    }

    @Transactional
    public BudgetResponseDto create(BudgetRequestDto request) {
        validateDates(request);

        User currentUser = authenticationService.getCurrentUser();
        Budget budget = budgetMapper.toEntity(request, currentUser);

        updateStatus(budget);

        Budget savedBudget = budgetRepository.save(budget);
        return budgetMapper.toResponse(savedBudget);
    }

    public List<BudgetResponseDto> listAllForCurrentUser() {
        User currentUser = authenticationService.getCurrentUser();

        return budgetRepository
                .findAllByUserAndDeletedAtIsNull(currentUser)
                .stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    public BudgetResponseDto getOwnById(Long id) {
        return budgetMapper.toResponse(findOwnBudget(id));
    }

    @Transactional
    public BudgetResponseDto update(
            Long id,
            BudgetRequestDto request) {
        validateDates(request);

        Budget budget = findOwnBudget(id);
        budgetMapper.updateEntity(budget, request);

        updateStatus(budget);

        Budget updatedBudget = budgetRepository.save(budget);
        return budgetMapper.toResponse(updatedBudget);
    }

    @Transactional
    public void deleteOwnById(Long id) {
        Budget budget = findOwnBudget(id);
        budget.setDeletedAt(new Date());
        budgetRepository.save(budget);
    }

    private Budget findOwnBudget(Long id) {
        User currentUser = authenticationService.getCurrentUser();

        return budgetRepository
                .findByIdAndUserAndDeletedAtIsNull(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Budget not found."));
    }

    private void validateDates(BudgetRequestDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The end date cannot be before the start date.");
        }
    }

    private void updateStatus(Budget budget) {
        BigDecimal spending = budget.getCurrentSpending() == null
                ? BigDecimal.ZERO
                : budget.getCurrentSpending();

        budget.setCurrentSpending(spending);

        if (spending.compareTo(budget.getMonthlyLimit()) > 0) {
            budget.setStatus(BudgetStatus.EXCEEDED);
        } else if (budget.getEndDate().isBefore(LocalDate.now())) {
            budget.setStatus(BudgetStatus.EXPIRED);
        } else if (budget.getStartDate().isAfter(LocalDate.now())) {
            budget.setStatus(BudgetStatus.INACTIVE);
        } else if (budget.getStatus() == null) {
            budget.setStatus(BudgetStatus.ACTIVE);
        }
    }
}