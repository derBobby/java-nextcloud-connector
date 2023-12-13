package eu.planlos.javanextcloudconnector.model;

import lombok.Getter;

import java.io.Serial;

@Getter
public class NextcloudException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 0L;

    public static final String IS_NULL = "The result from the Pretix API was NULL";
    public static final String USERID_NOT_POSSIBLE = "No free userid can be generated";
    public static final String EMAIL_ADDRESS_IN_USE = "Email address already in use";

    public NextcloudException(String message) {
        super(message);
    }
}