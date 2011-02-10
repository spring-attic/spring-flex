SPRING BLAZEDS INTEGRATION 1.5.0.M2 (February 2011)
----------------------------------
http://www.springsource.org/spring-flex

1. INTRODUCTION
---------------
Spring BlazeDS Integration is a top-level Spring project, and a component of the complete Spring Web stack.  The project's purpose is to make it easier to build Spring-powered 
Rich Internet Applications using Adobe Flex as the front-end client.  It aims to achieve this purpose by providing first-class support for using the open source Adobe BlazeDS 
project and its powerful remoting and messaging facilities in combination with the familiar Spring programming model.

2. RELEASE NOTES
----------------
Spring BlazeDS Integration requires Java SE 5.0, Spring Framework 3.0.0, and Adobe BlazeDS 4.0.0 or above to run.

Java SE 5 with Maven 2.2.0 or above is required to build.

Release distribution contents:

"." contains the Spring BlazeDS Integration distribution readme, license, changelog, and copyright
"dist" contains the Spring BlazeDS Integration distribution jar files
"src" contains the Spring BlazeDS Integration distribution source jar files
"docs" contains the Spring BlazeDS Integration reference manual and API Javadocs
"lib" (only in the with-dependencies distribution) contains all additional dependency jars, both mandatory and optional
"projects" contains buildable source projects for the individual components
"samples" contains buildable sample applications

Spring BlazeDS Integration is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES
-------------------------
The following jar files are included in the distribution.
The contents of each jar and its dependencies are noted.
Dependencies in [brackets] are optional, and are just necessary for certain functionality.

* spring-flex-core-1.5.0.M2.jar
- Contents: The Spring BlazeDS Integration core library, containing Spring's integration for Adobe BlazeDS.
- Runtime Dependencies ([] indicates optional) : Spring Core, Spring MVC, BlazeDS, Commons Logging and HttpClient, CGLib, Backport of javax.util.concurrent, 
[Spring Security], [Spring Integration], [Spring JMS], [Jackson]    

For an exact list of project dependencies including version numbers, see each project's pom file at "projects/${project_name}/pom.xml"