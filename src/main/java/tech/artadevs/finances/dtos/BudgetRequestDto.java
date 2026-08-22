package tech.artadevs.finances.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;

@Getter
@Setter
@Accessors(chain = true)
public class BudgetRequestDto {

    @NotBlank(message = "The budget name is required.")
    @Size(max = 100, message = "The budget name must have at most 100 characters.")
    private String name;

    @NotNull(message = "The monthly limit is required.")
    @DecimalMin(value = "0.01", message = "The monthly limit must be greater than zero.")
    @Digits(integer = 17, fraction = 2, message = "The monthly limit must have at most 17 integer digits and 2 decimal places.")
    private BigDecimal monthlyLimit;

    @DecimalMin(value = "0.00", message = "The current spending cannot be negative.")
    @Digits(integer = 17, fraction = 2, message = "The current spending must have at most 17 integer digits and 2 decimal places.")
    private BigDecimal currentSpending = BigDecimal.ZERO;

    @NotNull(message = "The budget category is required.")
    private BudgetCategory category;

    @NotNull(message = "The start date is required.")
    private LocalDate startDate;

    @NotNull(message = "The end date is required.")
    private LocalDate endDate;

    private BudgetStatus status = BudgetStatus.ACTIVE;
}