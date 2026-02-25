package ru.sportmaster.mpadapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

@Accessors(fluent = true)
@Entity
@Table(name = "planners")
public class Planner {

    Planner() {
    }

    public Planner(@NotNull @Validated List<Story> stories) {
        this(new PlannerName("My budget"), stories);
    }

    public Planner(@NotNull @Validated PlannerName name, @NotNull @Validated List<Story> stories) {
        this.id = new PlannerId(UUID.randomUUID());
        this.name = name;
        this.stories = stories;
    }

    @Getter
    @EmbeddedId
    private PlannerId id;
    @Getter
    @Embedded
    private PlannerName name;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Story> stories;
    @Version
    private Long version;

    public List<Story> stories() {
        return stories.stream().sorted(Comparator.comparing(Story::date)).toList();
    }

    public void updateStoryDate(@NotNull @Validated LocalDate aDate, int storyNumber) {
        stories().get(storyNumber).date(aDate);
    }

    public void updateStoryBudgetChange(@NotNull @Validated BudgetChange budgetChange,
                                        int storyNumber) {
        stories().get(storyNumber).change(budgetChange);
    }

    public void updateStoryComment(@NotNull @Validated StoryComment comment, int storyNumber) {
        stories().get(storyNumber).comment(comment);
    }

    public BigDecimal calcBudgetForDate(@NotNull @Validated LocalDate date) {
        var storiesForDate = stories
                .stream()
                .filter(story -> story.date().isBefore(date) || story.date().isEqual(date))
                .sorted(Comparator.comparing(Story::date))
                .toList();

        return storiesForDate
                .stream()
                .map(Story::change)
                .map(BudgetChange::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addStory(@NotNull @Validated Story story) {
        stories.add(story);
    }

    public void removeStory(@NotNull @Validated StoryId storyId) {
        stories.removeIf(story -> story.id().equals(storyId));
    }

    public void setPlannerName(@NotNull @Validated PlannerName plannerName) {
        name = plannerName;
    }
}
