package ru.sportmaster.mpadapter;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class GetPlannerUseCase {

    private final PlannerRepository plannerRepository;

    public Planner execute(@NotNull @Validated PlannerId plannerId) {
        return plannerRepository.findById(plannerId).orElseThrow();
    }

}
