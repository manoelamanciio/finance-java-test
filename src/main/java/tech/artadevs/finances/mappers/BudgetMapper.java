package tech.artadevs.finances.mappers;

import org.springframework.stereotype.Component;

import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.models.Budget;
import tech.artadevs.finances.models.User;

@Component
public class BudgetMapper {

    public Budget toEntity(
            BudgetRequestDto request,
            User user) {
        return new Budget()
                .setName(request.getName())
                .setMonthlyLimit(request.getMonthlyLimit())
                .setCurrentSpending(request.getCurrentSpending())
                .setCategory(request.getCategory())
                .setStartDate(request.getStartDate())
                .setEndDate(request.getEndDate())
                .setStatus(request.getStatus())
                .setUser(user);
    }

    public Budget updateEntity(
            Budget budget,
            BudgetRequestDto request) {
        return budget
                .setName(request.getName())
                .setMonthlyLimit(request.getMonthlyLimit())
                .setCurrentSpending(request.getCurrentSpending())
                .setCategory(request.getCategory())
                .setStartDate(request.getStartDate())
                .setEndDate(request.getEndDate())
                .setStatus(request.getStatus());
    }

    public BudgetResponseDto toResponse(Budget budget) {
        return new BudgetResponseDto()
                .setId(budget.getId())
                .setName(budget.getName())
                .setMonthlyLimit(budget.getMonthlyLimit())
                .setCurrentSpending(budget.getCurrentSpending())
                .setRemainingBalance(budget.getRemainingBalance())
                .setCategory(budget.getCategory())
                .setStartDate(budget.getStartDate())
                .setEndDate(budget.getEndDate())
                .setStatus(budget.getStatus())
                .setCreatedAt(budget.getCreatedAt())
                .setUpdatedAt(budget.getUpdatedAt());
    }
}