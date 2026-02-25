package ru.sportmaster.mpadapter;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class DeleteStoryUseCase {

    private final PlannerRepository plannerRepository;

    @Transactional
    public void execute(@NotNull @Validated PlannerId plannerId,
                        @NotNull @Validated StoryId storyId) {
        Planner planner = plannerRepository.findById(plannerId).orElseThrow();

        planner.removeStory(storyId);

        plannerRepository.save(planner);
    }

}
