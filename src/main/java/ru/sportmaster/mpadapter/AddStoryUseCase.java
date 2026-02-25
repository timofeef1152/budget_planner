package ru.sportmaster.mpadapter;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class AddStoryUseCase {

    private final PlannerRepository plannerRepository;

    @Transactional
    public void execute(@NotNull @Validated PlannerId plannerId, @NotNull @Validated Story story) {
        Planner planner = plannerRepository.findById(plannerId).orElseThrow();

        planner.addStory(story);

        plannerRepository.save(planner);
    }

}
