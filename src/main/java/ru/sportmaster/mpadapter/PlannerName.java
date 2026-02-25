package ru.sportmaster.mpadapter;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

public record PlannerName(@NotNull @NotBlank @Validated @Column(name = "name") String value) {

}
