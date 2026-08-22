package tech.artadevs.finances.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;

@Getter
@Setter
@Accessors(chain = true)
public class BudgetResponseDto {

    private Long id;
    private String name;
    private BigDecimal monthlyLimit;
    private BigDecimal currentSpending;
    private BigDecimal remainingBalance;
    private BudgetCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetStatus status;
    private Date createdAt;
    private Date updatedAt;
}