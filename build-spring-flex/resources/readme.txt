SPRING BLAZEDS INTEGRATION 1.0.0.M1 (December 2008)
----------------------------------
http://www.springframework.org/flex

1. INTRODUCTION
---------------
Spring BlazeDS Integration is 

2. RELEASE NOTES
----------------
Spring BlazeDS Integration requires Java SE 1.4 and Spring Framework 2.5.0 or above to run.

Java SE 1.4 with Ant 1.7 is required to build.

Release distribution contents:

"." contains the Spring BlazeDS Integration distribution readme, license, changelog, and copyright
"dist" contains the Spring BlazeDS Integration distribution jar files
"src" contains the Spring BlazeDS Integration distribution source jar files
"docs" contains the Spring BlazeDS Integration reference manual and API Javadocs
"projects" contains all buildable projects, including sample applications
"projects/build-spring-flex" is the directory to access to build the Spring BlazeDS Integration distribution
"projects/spring-build" is the master build system used by all Spring projects, including Spring BlazeDS Integration
"projects/org.springframework.flex" contains buildable Spring BlazeDS Integration project sources

See the readme.txt within the above directories for additional information.

Spring BlazeDS Integration is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES
-------------------------
The following jar files are included in the distribution.
The contents of each jar and its dependencies are noted.
Dependencies in [brackets] are optional, and are just necessary for certain functionality.

* org.springframework.flex-1.0.0.M1.jar
- Contents: The Spring BlazeDS Integration library, containing Spring's integration for Adobe BlazeDS.
- Dependencies: Spring MVC, BlazeDS

For an exact list of project dependencies, see each project's ivy file at "projects/${project_name}/ivy.xml".