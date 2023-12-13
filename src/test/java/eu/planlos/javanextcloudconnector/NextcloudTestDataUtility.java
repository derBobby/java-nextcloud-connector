package eu.planlos.javanextcloudconnector;

import eu.planlos.javanextcloudconnector.model.NextcloudMeta;
import eu.planlos.javanextcloudconnector.model.NextcloudUser;

import java.util.ArrayList;

public class NextcloudTestDataUtility {

    public static NextcloudUser takenUser(String accountNamePrefix, String accountNameSuffix) {
        return new NextcloudUser(
                String.format("%sdname%s", accountNamePrefix, accountNameSuffix),
                "Display Name",
                "dname@example.com",
                new ArrayList<>(),
                true);
    }

    public static NextcloudMeta okMeta() {
        return new NextcloudMeta("200",
                200,
                "All fine",
                null,
                null);
    }
}