package eu.planlos.javanextcloudconnector.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NextcloudApiResponse<T> {
    @NotNull
    private NextcloudMeta meta;
    @NotNull
    private T data;
}