# Simple utilities for working with a Git codebase 

![CI](https://github.com/joshlong/git-spring-boot-starter/workflows/CI/badge.svg)

Some utilities to make working with Git a little easier from within my Spring applications.


## Usage: 

Assuming the code is `mvn clean install`-ed into your Maven repository, add the following dependency to your project: 

```xml
<dependency>
    <groupId>com.joshlong</groupId>
    <artifactId>
     git-spring-boot-starter
    </artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```



This module provides a Spring Boot autoconfiguration that does most of the work, though you'll need to account for somethings. 

You'll need to then specify how to connect to a Git repository. 

You can specify a lot of the important peices using Spring Boot configuration properties. Here are some key ones: 

-  `git.http.username` - the username for the HTTP(S) connection to be made to a Git repository 
-  `git.http.password` - the password for the HTTP(S) connection to be made to a Git repository. Obviously, some care should be taken to store this particular value in a secure context like Hashicorp Vault or your cloud provider's key vault. 
- `git.uri` - which Git repository to connect to 

If you have particular connection requirements, you might override the Spring bean definitions of `JGit`'s `Git` client, or the `PushCommandCreator` 

Assuming everything's functioning, you can inject a reference to a `GitTemplate`, which makes certain usecases more convenient. 

```java

@Configuration 
class MyGitConfiguration {
 
 @Bean 
 ApplicationListener<ApplicationReadyEvent> client(GitTemplate gt){ 
   return event -> gt.execute ( git -> git.clone()  ) ;
 }
}
```



