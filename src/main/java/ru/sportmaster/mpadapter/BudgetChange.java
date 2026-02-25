package ru.sportmaster.mpadapter;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

public record BudgetChange(@NotNull @Validated @Column(name = "budget_change") BigDecimal value) {

}
