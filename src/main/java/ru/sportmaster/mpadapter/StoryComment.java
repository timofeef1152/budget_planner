package ru.sportmaster.mpadapter;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

public record StoryComment(@NotNull @NotBlank @Validated @Column(name = "comment") String value) {

}
