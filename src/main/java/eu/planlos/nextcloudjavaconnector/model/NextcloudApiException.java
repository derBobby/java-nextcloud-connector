package eu.planlos.nextcloudjavaconnector.model;

import lombok.Getter;

import java.io.Serial;

@Getter
public class NextcloudApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 0L;

    public static final String IS_NULL = "NextcloudApiResponse object is NULL";

    public NextcloudApiException(String message) {
        super(message);
    }
}