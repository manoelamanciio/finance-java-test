package tech.artadevs.finances.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tech.artadevs.finances.models.enums.BudgetCategory;
import tech.artadevs.finances.models.enums.BudgetStatus;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "monthly_limit", precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlyLimit;

    @Column(name = "current_spending", precision = 19, scale = 2, nullable = false)
    private BigDecimal currentSpending = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BudgetCategory category;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BudgetStatus status = BudgetStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deleted_at")
    private Date deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Transient
    public BigDecimal getRemainingBalance() {
        if (monthlyLimit == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal spending = currentSpending == null
                ? BigDecimal.ZERO
                : currentSpending;

        return monthlyLimit.subtract(spending);
    }
}