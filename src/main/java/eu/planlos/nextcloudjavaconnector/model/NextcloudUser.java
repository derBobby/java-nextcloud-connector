package eu.planlos.nextcloudjavaconnector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NextcloudUser(String id, @JsonProperty("displayname") String displayName, String email,
                            List<String> groups, boolean enabled) {
}