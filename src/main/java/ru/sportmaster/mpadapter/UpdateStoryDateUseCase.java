package ru.sportmaster.mpadapter;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
public class UpdateStoryDateUseCase {

    private final PlannerRepository plannerRepository;

    @Transactional
    public void execute(@NotNull @Validated PlannerId plannerId,
                        @NotNull @Validated String value,
                        int storyNumber) {
        LocalDate date = LocalDate.parse(value);

        Planner planner = plannerRepository.findById(plannerId).orElseThrow();

        planner.updateStoryDate(date, storyNumber);
    }

}
