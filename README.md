[![Merge Dependabot PR](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/dependabot-automerge.yml/badge.svg)](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/dependabot-automerge.yml) [![CD](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/test-and-publish.yml/badge.svg)](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/test-and-publish.yml)

# java-nextcloud-connector
This spring boot library allows to connect to the Nextcloud OCS API v1.

## Supported API functions
* Fetch all userids from a Nextcloud installation
* Fetch all userid:email maps from a Nextcloud installation
* Create user

# Usage

## Maven setup
```xml
        <dependency>
            <groupId>eu.planlos</groupId>
            <artifactId>java-nextcloud-connector</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
```

## Config class setup
```java
@Configuration
@ComponentScan(basePackages = "eu.planlos.javanextcloudconnector")
public class NextcloudConfig {}
```

## Properties setup
| Property                            | Type    | Description                                  |
|-------------------------------------|---------|----------------------------------------------|
| `nextcloud.api.active`              | Boolean | Enable/Disable usage of API                  |
| `nextcloud.api.address`             | String  | Nextcloud URL                                |
| `nextcloud.api.user`                | String  | Nextcloud Username                           | 
| `nextcloud.api.password`            | String  | Nextcloud password                           | 
| `nextcloud.api.defaultgroup`        | String  | default group for user creation              | 
| `nextcloud.api.retry-count`         | String  | Retry count in case of exception             | 
| `nextcloud.api.retry-interval`      | String  | PInterval for retries in case of exception   | 
| `nextcloud.api.account-name-prefix` | String  | Prefix to be used for a created account name | 
| `nextcloud.api.account-name-suffix` | String  | Suffix to be used for a created account name | 

## Use in your code
Autowire the SignalService
```java
class YourClass {
    @Autowired
    private final NextcloudApiUserService ncUserService;
}
```

Call service to use API functions
```java
class YourClass {
    yourMethod() {
        try {
            // Your code
            List<String> list = ncUserService.getAllUserIdsFromNextcloud();
            Map<String, String> map = ncUserService.getAllUsersAsUseridEmailMap();
            String username = createUser(email, firstName, lastName);
            // Your code
        } catch(NextcloudException e) {
            // your code
}   }   }
```