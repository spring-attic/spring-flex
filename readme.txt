SPRING BLAZEDS INTEGRATION 1.6.0.BUILD-SNAPSHOT
-----------------------------------------------

1. INTRODUCTION
---------------
Spring BlazeDS Integration is a top-level Spring project, and a component of the complete Spring Web stack.  The project's purpose is to make it easier to build Spring-powered 
Rich Internet Applications using Adobe Flex as the front-end client.  It aims to achieve this purpose by providing first-class support for using the open source Adobe BlazeDS 
project and its powerful remoting and messaging facilities in combination with the familiar Spring programming model.

2. RELEASE NOTES
----------------
Spring BlazeDS Integration requires Java SE 6.0, Spring Framework 4.0.0, and Adobe BlazeDS 4.0.0 or above to run.

Java SE 6 with Maven 3.0 or above is required to build.

Release distribution contents:

"." contains the Spring BlazeDS Integration distribution readme, license, changelog, and copyright
"dist" contains the Spring BlazeDS Integration distribution jar files
"src" contains the Spring BlazeDS Integration distribution source jar files
"docs" contains the Spring BlazeDS Integration reference manual and API Javadocs
"projects" contains buildable source projects for the individual components
"samples" contains buildable sample applications

Spring BlazeDS Integration is released under the terms of the Apache Software License (see license.txt).

3. ISSUE TRACKING
-----------------
Please report issues via the Spring Flex JIRA at https://jira.spring.io/browse/FLEX

4. DISTRIBUTION JAR FILES
-------------------------
The following jar files are included in the distribution.
The contents of each jar and its dependencies are noted.
Dependencies in [brackets] are optional, and are just necessary for certain functionality.

* spring-flex-core-1.5.1.RELEASE.jar
- Contents: The Spring BlazeDS Integration core library, containing Spring's integration for Adobe BlazeDS.
- Runtime Dependencies ([] indicates optional) : Spring Core, Spring MVC, BlazeDS, Commons Logging and HttpClient, CGLib, Backport of javax.util.concurrent, 
[Spring Security], [Spring Integration], [Spring JMS], [Jackson]    

For an exact list of project dependencies including version numbers, refer to the following output from the Maven Dependency Plugin as of the release 
(this lists *all* dependencies, including those that are transitive, optional, and provided):

[INFO] ------------------------------------------------------------------------
[INFO] Building Spring BlazeDS Integration Core
[INFO]    task-segment: [dependency:list]
[INFO] ------------------------------------------------------------------------
[INFO] [dependency:list {execution: default-cli}]
[INFO] 
[INFO] The following files have been resolved:
[INFO]    org.springframework:spring-core:jar:4.0.6.RELEASE:compile
[INFO]    org.springframework:spring-jms:jar:4.0.6.RELEASE:compile
[INFO]    com.h2database:h2:jar:1.4.180:test
[INFO]    org.springframework.integration:spring-integration-core:jar:2.2.6.RELEASE:compile
[INFO]    org.springframework:spring-web:jar:4.0.6.RELEASE:compile
[INFO]    org.springframework.security:spring-security-config:jar:3.2.4.RELEASE:compile
[INFO]    org.springframework.retry:spring-retry:jar:1.0.2.RELEASE:compile
[INFO]    org.hamcrest:hamcrest-core:jar:1.1:test
[INFO]    com.adobe.blazeds:blazeds-proxy:jar:4.0.0.14931:compile
[INFO]    commons-logging:commons-logging:jar:1.1.1:compile
[INFO]    cglib:cglib-nodep:jar:3.1:compile
[INFO]    org.springframework:spring-tx:jar:4.0.6.RELEASE:compile
[INFO]    org.apache.activemq.protobuf:activemq-protobuf:jar:1.1:test
[INFO]    xml-apis:xml-apis:jar:1.3.04:test
[INFO]    org.fusesource.hawtbuf:hawtbuf:jar:1.9:test
[INFO]    org.slf4j:slf4j-api:jar:1.7.7:compile
[INFO]    org.springframework:spring-expression:jar:4.0.6.RELEASE:compile
[INFO]    junit:junit:jar:4.9:test
[INFO]    log4j:log4j:jar:1.2.17:test
[INFO]    commons-httpclient:commons-httpclient:jar:3.1:compile
[INFO]    commons-codec:commons-codec:jar:1.2:compile
[INFO]    javassist:javassist:jar:3.12.0.GA:compile
[INFO]    javax.servlet:javax.servlet-api:jar:3.0.1:provided
[INFO]    org.jasypt:jasypt:jar:1.9.0:test
[INFO]    org.springframework:spring-test:jar:4.0.6.RELEASE:test
[INFO]    org.springframework:spring-context:jar:4.0.6.RELEASE:compile
[INFO]    org.springframework:spring-webmvc:jar:4.0.6.RELEASE:compile
[INFO]    xalan:xalan:jar:2.7.1:test
[INFO]    org.apache.geronimo.specs:geronimo-j2ee-management_1.1_spec:jar:1.0.1:test
[INFO]    aopalliance:aopalliance:jar:1.0:compile
[INFO]    org.springframework:spring-jdbc:jar:4.0.6.RELEASE:test
[INFO]    org.springframework:spring-orm:jar:4.0.6.RELEASE:test
[INFO]    javax.transaction:jta:jar:1.1:compile
[INFO]    javax.jms:jms-api:jar:1.1-rev-1:provided
[INFO]    org.fusesource.mqtt-client:mqtt-client:jar:1.3:test
[INFO]    org.springframework.security:spring-security-core:jar:3.2.4.RELEASE:compile
[INFO]    org.hibernate:hibernate-commons-annotations:jar:3.2.0.Final:compile
[INFO]    org.slf4j:slf4j-log4j12:jar:1.7.7:test
[INFO]    antlr:antlr:jar:2.7.6:compile
[INFO]    javax.annotation:jsr250-api:jar:1.0:provided
[INFO]    cglib:cglib:jar:2.2:compile
[INFO]    org.hibernate:hibernate-entitymanager:jar:3.6.10.Final:compile
[INFO]    xalan:serializer:jar:2.7.1:test
[INFO]    commons-collections:commons-collections:jar:3.1:compile
[INFO]    org.mockito:mockito-all:jar:1.8.2:test
[INFO]    org.apache.activemq:activemq-core:jar:5.7.0:test
[INFO]    org.springframework:spring-beans:jar:4.0.6.RELEASE:compile
[INFO]    org.springframework:spring-aop:jar:4.0.6.RELEASE:compile
[INFO]    org.springframework:spring-context-support:jar:4.0.6.RELEASE:compile
[INFO]    org.apache.geronimo.specs:geronimo-jms_1.1_spec:jar:1.1.1:test
[INFO]    com.adobe.blazeds:blazeds-common:jar:4.0.0.14931:compile
[INFO]    org.springframework.security:spring-security-web:jar:3.2.4.RELEASE:compile
[INFO]    org.fusesource.hawtdispatch:hawtdispatch-transport:jar:1.11:test
[INFO]    org.codehaus.jackson:jackson-core-asl:jar:1.9.13:compile
[INFO]    dom4j:dom4j:jar:1.6.1:compile
[INFO]    org.hibernate:hibernate-core:jar:3.6.10.Final:compile
[INFO]    org.hibernate.javax.persistence:hibernate-jpa-2.0-api:jar:1.0.1.Final:compile
[INFO]    com.adobe.blazeds:blazeds-remoting:jar:4.0.0.14931:compile
[INFO]    org.apache.activemq:kahadb:jar:5.7.0:test
[INFO]    com.adobe.blazeds:blazeds-core:jar:4.0.0.14931:compile
[INFO]    commons-net:commons-net:jar:3.1:test
[INFO]    org.fusesource.hawtdispatch:hawtdispatch:jar:1.11:test
[INFO]    asm:asm:jar:3.1:compile

