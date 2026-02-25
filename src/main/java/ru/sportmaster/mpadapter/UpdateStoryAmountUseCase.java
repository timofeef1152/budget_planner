package ru.sportmaster.mpadapter;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class UpdateStoryAmountUseCase {

    private final PlannerRepository plannerRepository;

    @Transactional
    public void execute(@NotNull @Validated PlannerId plannerId,
                        @NotNull @Validated String value,
                        int storyNumber) {
        BigDecimal amount = new BigDecimal(value);

        Planner planner = plannerRepository.findById(plannerId).orElseThrow();

        BudgetChange budgetChange = new BudgetChange(amount);
        planner.updateStoryBudgetChange(budgetChange, storyNumber);
    }
}
