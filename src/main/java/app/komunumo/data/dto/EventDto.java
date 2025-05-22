package app.komunumo.data.dto;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public record EventDto(
        @Nullable UUID id,
        @Nullable UUID communityId,
        @NotNull String title
) {
}
