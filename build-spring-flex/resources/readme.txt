SPRING BLAZEDS INTEGRATION 1.0.2.RELEASE (February 2010)
----------------------------------
http://www.springsource.org/projects/blaze-ds-integration

1. INTRODUCTION
---------------
Spring BlazeDS Integration is a new addition to the Spring Portfolio that provides first-class support for building
Spring-powered RIAs using Adobe Flex.

2. RELEASE NOTES
----------------
Spring BlazeDS Integration requires Java SE 5.0, Spring Framework 2.5.6, and Adobe BlazeDS 3.2.0 or above to run.

Java SE 5 with Ant 1.7 is required to build.

Release distribution contents:

"." contains the Spring BlazeDS Integration distribution readme, license, changelog, and copyright
"dist" contains the Spring BlazeDS Integration distribution jar files
"src" contains the Spring BlazeDS Integration distribution source jar files
"docs" contains the Spring BlazeDS Integration reference manual and API Javadocs
"projects" contains all buildable projects, including sample applications

Spring BlazeDS Integration is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES
-------------------------
The following jar files are included in the distribution.
The contents of each jar and its dependencies are noted.
Dependencies in [brackets] are optional, and are just necessary for certain functionality.

* org.springframework.flex-1.0.2.RELEASE.jar
- Contents: The Spring BlazeDS Integration library, containing Spring's integration for Adobe BlazeDS.
- Runtime Dependencies: Spring Core, Spring MVC, BlazeDS, Jackson, Commons Logging and HttpClient, CGLib, Backport of javax.util.concurrent, 
[Spring Security], [Spring Integration], [Spring JMS]    

For an exact list of project dependencies including version numbers, see each project's ivy file at "projects/${project_name}/ivy.xml" or pom file at "projects/${project_name}/pom.xml"