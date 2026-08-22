
package tech.artadevs.finances.repositories;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tech.artadevs.finances.models.FinancialTransaction;
import tech.artadevs.finances.models.User;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    @Query("SELECT ft FROM FinancialTransaction ft WHERE ft.user = :user AND ft.deletedAt IS NULL")
    List<FinancialTransaction> findByUser(User user);

    Optional<FinancialTransaction> findByIdAndUserAndDeletedAtIsNull(Long id, User user);

    @Query("SELECT SUM(ft.value) FROM FinancialTransaction ft WHERE ft.user = :user AND ft.deletedAt IS NULL")
    BigDecimal getUserTransactionsTotalValue(User user);
}
