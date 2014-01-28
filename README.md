# Eclipse Integration Common Components
      
  The Eclipse Integration Common Components are a set of plugins and features that are used and
  consumed by other Eclipse tooling projects at SpringSource. It contains the commonly used parts
  and provides two individually installable features:

  Spring UAA (User Agent Analysis): The Eclipse integration for the Spring UAA project that helps
  us to collect some usage data. This is completely anonymous and helps us to understand better how
  the tooling is used and how to improve it in the future.

  The SpringSource Dashboard feature brings you up-to-date information about SpringSource-related
  projects as well as an easy-to-use extension install to get additional tooling add-ons, like the
  famous Spring IDE or the Cloud Foundry Integration for Eclipse.

  Both components are usually optional features of the other end-used-oriented tooling projects.

## Installation

  Usually you don't need to install these components directly from this project yourself. They come
  as part of Spring IDE, the tc Server integration, the Grails IDE, or the Cloud Foundry Integration
  for Eclipse.

  However, if you want to install those features yourself, you could use the following update
  sites manually:

  CI builds: http://dist.springsource.com/snapshot/TOOLS/eclipse-integration-commons/nightly  
  Milestone builds: http://dist.springsource.com/milestone/TOOLS/eclipse-integration-commons/  
  Release builds: http://dist.springsource.com/release/TOOLS/eclipse-integration-commons/
  
### JavaFx Setup

  The browser projects use JavaFx to implement the dashboard and other managed html views. To build these, you'll need to have the e(fx)clipse tooling installed.
  
  1. Add e(fx)clipse Update site: http://download.eclipse.org/efxclipse/updates-nightly/site
  2. Add Xtext update site: http://download.eclipse.org/modeling/tmf/xtext/updates/composite/releases/
  3. Install the "e(fx)clipse - IDE - Kepler" (or equivalent) feature.

  If installed properly, you should see a "JavaFx" library with an attached jar in the org.springsource.ide.eclipse.commons.gettingstarted (to be moved) project.  

  4. Add the following vm arguments to your Eclipse Runtime(s). (This is only necessary for self-hosted execution. You should not need to modify your config.ini file.)

      -Dosgi.framework.extensions=org.eclipse.fx.osgi
  
## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the forum:

  http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite

  There you can also ask questions and search for other people with related or similar problems
  (and solutions). New versions are announced there as well, usually as part of the releases
  of the consuming projects.

  With regards to bug reports, please go to:

  https://issuetracker.springsource.com/browse/STS

## Developing Eclipse Integration Common Components

  Just clone the repo and import the projects into an Eclipse workspace. The easiest way to ensure
  that your target platform contains all the necessary dependencies, install a CI build into
  your target platform and proceed.

## Building Eclipse Integration Common Components
  
  The Eclipse Integration Common Components project uses Maven Tycho to do continuous integration
  builds and to produce p2 repos and update sites. To build the project yourself, you can execute:

  `mvn -Pe37 -Dmaven.test.skip=true clean install`

## Contributing

  Here are some ways for you to get involved in the community:

  * Get involved with the Spring community on the Spring Community Forums.  Please help out on the [forum](http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite) by responding to questions and joining the debate.
  * Create [JIRA](https://issuetracker.springsource.com/browse/STS) tickets for bugs and new features and comment and vote on the ones that you are interested in.  
  * Github is for social coding: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please reference a JIRA ticket as well covering the specific issue you are addressing.
  * Watch for upcoming articles on Spring by [subscribing](http://www.springsource.org/node/feed) to springframework.org

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_eclipsecla_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do. Active contributors might be asked to join the core team, and given the ability to merge pull requests.
