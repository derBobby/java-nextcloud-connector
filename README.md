# java-nextcloud-connector
This spring boot library allows to connect to the Nextcloud OCS API v1.

## Features
### Users endpoint
* Fetch all userids
* Fetch userid:email maps
* Fetch user object
* Create user

## Usage
Add Maven dependency
```xml
        <dependency>
            <groupId>eu.planlos</groupId>
            <artifactId>java-nextcloud-connector</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
```

Add Configuration class
```java
@Configuration
@ComponentScan(basePackages = "eu.planlos.javanextcloudconnector")
public class NextcloudConfig {}
```

## Properties

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

## Status

[![Merge Dependabot PR](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/dependabot-automerge.yml/badge.svg)](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/dependabot-automerge.yml)

[![CD](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/test-and-publish.yml/badge.svg)](https://github.com/derBobby/java-nextcloud-connector/actions/workflows/test-and-publish.yml)
