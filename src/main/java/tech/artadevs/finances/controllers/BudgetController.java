package tech.artadevs.finances.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import tech.artadevs.finances.dtos.BudgetRequestDto;
import tech.artadevs.finances.dtos.BudgetResponseDto;
import tech.artadevs.finances.services.BudgetService;

@Validated
@RestController
@RequestMapping("/user/me/budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponseDto createBudget(
            @Valid @RequestBody BudgetRequestDto request) {
        return budgetService.create(request);
    }

    @GetMapping
    public List<BudgetResponseDto> listBudgets() {
        return budgetService.listAllForCurrentUser();
    }

    @GetMapping("/{id}")
    public BudgetResponseDto getBudgetById(
            @PathVariable Long id) {
        return budgetService.getOwnById(id);
    }

    @PutMapping("/{id}")
    public BudgetResponseDto updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDto request) {
        return budgetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(@PathVariable Long id) {
        budgetService.deleteOwnById(id);
    }
}