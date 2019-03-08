# Eclipse Integration Common Components
      
  The Eclipse Integration Common Components are a set of plugins and features that are used and
  consumed by other Eclipse tooling projects at SpringSource. It contains the commonly used parts
  and provides two individually installable features:

  The Spring Dashboard feature brings you up-to-date information about Spring-related
  projects as well as an easy-to-use extension install to get additional tooling add-ons, like the
  famous Spring IDE or the Cloud Foundry Integration for Eclipse.

  Both components are usually optional features of the other end-used-oriented tooling projects.

## Installation

  Usually you don't need to install these components directly from this project yourself. They come
  as part of Spring IDE, the tc Server integration, or the Cloud Foundry Integration
  for Eclipse.

  However, if you want to install those features yourself, you could use the following update
  sites manually:

  For Eclipse >= 4.7:
  
    - CI builds: https://dist.springsource.com/snapshot/TOOLS/eclipse-integration-commons/nightly/ 
    - Milestone builds: https://dist.springsource.com/milestone/TOOLS/eclipse-integration-commons/
    - Release builds: https://dist.springsource.com/release/TOOLS/eclipse-integration-commons/

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the stackoverflow
  using the tag `spring-tool-suite`:

  https://stackoverflow.com/tags/spring-tool-suite[`spring-tool-suite`]
  
  Bug reports and enhancement requests are tracked using GitHub issues here:
  
  https://github.com/spring-projects/eclipse-integration-commons/issues

## Developing Eclipse Integration Common Components

  Just clone the repo and import the projects into an Eclipse workspace. The easiest way to ensure
  that your target platform contains all the necessary dependencies, install a CI build into
  your target platform and proceed.
  
### Test dependencies

If you are not interested in building/running JUnit tests in your development environment then you can skip this step and just close/ignore any xxx.xxx.test plugins that have errors in them.

If you want to build and run tests inside of Eclipse then you'll need to add a few more things to your target platform:

   - TODO: fill this in


## Building Eclipse Integration Common Components
  
  The Eclipse Integration Common Components project uses Maven Tycho to do continuous integration
  builds and to produce p2 repos and update sites. To build the project yourself, you can execute:

  `mvn -Pe47 -Dmaven.test.skip=true clean install`
