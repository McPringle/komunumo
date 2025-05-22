package app.komunumo.data.dto;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record EventDto(
        @Nullable UUID id,
        @Nullable UUID communityId,
        @NotNull String title,
        @Nullable UUID imageId
) {
}
