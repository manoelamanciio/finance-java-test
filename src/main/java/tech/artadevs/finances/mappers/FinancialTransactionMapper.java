package tech.artadevs.finances.mappers;

import org.springframework.stereotype.Component;

import tech.artadevs.finances.dtos.FinancialTransactionResponseDto;
import tech.artadevs.finances.models.FinancialTransaction;

@Component
public class FinancialTransactionMapper {

    public FinancialTransactionResponseDto toResponse(
            FinancialTransaction financialTransaction) {
        return new FinancialTransactionResponseDto()
                .setId(financialTransaction.getId())
                .setValue(financialTransaction.getValue())
                .setDescription(financialTransaction.getDescription())
                .setCreatedAt(financialTransaction.getCreatedAt())
                .setUpdatedAt(financialTransaction.getUpdatedAt());
    }
}