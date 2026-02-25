package ru.sportmaster.mpadapter;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

public record PlannerId(@NotNull @Validated @Column(name = "id") UUID value) {

}
