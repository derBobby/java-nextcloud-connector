package eu.planlos.javanextcloudconnector.model;

import lombok.Getter;

import java.io.Serial;

@Getter
public class NextcloudException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 0L;

    public static final String IS_NULL = "The result from the Pretix API was NULL";

    public NextcloudException(String message) {
        super(message);
    }
}