package tech.artadevs.finances.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tech.artadevs.finances.models.Budget;
import tech.artadevs.finances.models.User;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByUserAndDeletedAtIsNull(User user);

    Optional<Budget> findByIdAndUserAndDeletedAtIsNull(
            Long id,
            User user);
}