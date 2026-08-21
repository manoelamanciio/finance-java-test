package tech.artadevs.finances.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FinancialTransactionRequestDto {

	@NotNull(message = "The transaction value is required.")
	private BigDecimal value;

	@Size(max = 100, message = "The length of description must be at most 100 characters.")
	private String description;

	public FinancialTransactionRequestDto() {
	}

	public BigDecimal getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public FinancialTransactionRequestDto setValue(BigDecimal value) {
		this.value = value;
		return this;
	}

	public FinancialTransactionRequestDto setDescription(String description) {
		this.description = description;
		return this;
	}
}