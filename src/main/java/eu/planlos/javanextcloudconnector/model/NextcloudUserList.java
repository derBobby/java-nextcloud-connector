package eu.planlos.javanextcloudconnector.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class NextcloudUserList {
    @NotNull
    private List<String> users;

    public NextcloudUserList() {
        this.users = new ArrayList<>();
    }
}