THIS PROJECT IS NOT ACTIVE ANYMORE !

If you still have projects running in production, be aware that BlazeDS 4.7.2 and earlier
are vulnerable to CVE-2017-5641. Even if this project is not maintained anymore, you
should be able to use BlazeDS 4.7.3+ by using this updated AmfHttpMessageConverter (copy
it in your project with a different package) that allows to specify a DeserializationValidator:
https://github.com/spring-projects/spring-flex/blob/apache-flex/spring-flex-core/src/main/java/org/springframework/flex/http/AmfHttpMessageConverter.java


SPRING BLAZEDS INTEGRATION 1.6
------------------------------

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
You will find issues one the Spring Flex JIRA at https://jira.spring.io/browse/FLEX

4. DISTRIBUTION JAR FILES
-------------------------

Spring Flex snapshot builds are available from http://repo.spring.io/libs-snapshot/
SPring Flex release candidates are available from http://repo.spring.io/libs-milestone/
Spring Flex release builds are available from Maven Central.

They are using the org.springframework.flex groupId.



