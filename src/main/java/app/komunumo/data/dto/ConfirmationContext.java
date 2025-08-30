package app.komunumo.data.dto;

import org.jetbrains.annotations.NotNull;

public record ConfirmationContext(
        @NotNull String email
) { }
