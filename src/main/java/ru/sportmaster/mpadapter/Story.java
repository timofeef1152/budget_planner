package ru.sportmaster.mpadapter;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.annotation.Validated;

@Accessors(fluent = true)
@Entity
@Table(name = "stories")
public class Story {

    @Getter
    @EmbeddedId
    private StoryId id;
    @Setter
    @Getter
    private LocalDate date;
    @Setter
    @Getter
    @Embedded
    private BudgetChange change;
    @Setter
    @Getter
    @Embedded
    private StoryComment comment;
    @Version
    private Long version;

    Story() {
    }

    public Story(@NotNull @Validated LocalDate date,
                 @NotNull @Validated BudgetChange change,
                 @NotNull @Validated StoryComment comment) {
        this.id = new StoryId(UUID.randomUUID());
        this.date = date;
        this.change = change;
        this.comment = comment;
    }
}
