package ru.sportmaster.mpadapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class CalculateBalanceForDateUseCase {

    private final PlannerRepository plannerRepository;

    public BigDecimal execute(@NotNull @Validated PlannerId plannerId,
                              @NotNull @Validated LocalDate date) {
        Planner planner = plannerRepository.findById(plannerId).orElseThrow();

        return planner.calcBudgetForDate(date);
    }

}
