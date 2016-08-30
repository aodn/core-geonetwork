IntelliJ Setup
--------------

### Clone project files

    git clone --recursive git@github.com:aodn/core-geonetwork.git

### Import project files

* Import project 
* Select core-geonetwork directory created above
* Select Import project from existing model
* Select Maven
* Check Search for Projects Recursively
* Check Import Maven projects automatically
* Check create module groups for multi-module maven projects
* Hit next and continue hitting next until project is created
* Don't add iml files to git if asked

### Setup intellij to deploy and run GeoNetwork in debug mode on tomcat

* Edit run configurations
* Create a new local tomcat server configuration
* Name configuration e.g.GeoNetwork
* Configure application server (e.g. local tomcat 1.7 install)
* Go to Deployment tab
* Add geonetwork-main:war exploded artifact
* Set geonetwork-main context to /geonetwork
* Add geoserver:war exploded artifact
* Set geoserver context to /geoserver
* Remove 'Make' and 'Build 2 artifacts' from Before Launch (intellij can't filter webapp resources which is used by GeoNetwork)
* Add 'Select maven goal' in Before Launch
  * select core-geonetwork directory for working directory
  * use install -DskipTests - for relative quick builds of the whole geonetwork project
* Hit OK

You should now be able to run or debug GeoNetwork running on the local tomcat install using this configuration 


Eclipse Setup
-------------

## Install Eclipse Luna (J2EE package)

## Import geonetwork clone

File > Import > Maven > Existing Maven Project

Select location of geonetwork workspace

For a non-imos clone you may get the following error

yui-compressor - resolve later 

This can be fixed by changing the pom refer 

http://stackoverflow.com/questions/6008942/yuicompressor-plugin-execution-not-covered-in-m2e

this is an eclipse specific fix which I'm not sure is appropriate for core

I have however added it to this branch for convenience in generating compressed java script files

Wait for build to finish or cancel as we are changing more build options below

## Disable validation of xml and xsl files on build

Validating xml/xsl files takes a long time so I'd recommend disabling validation on build and validating manually when required

Windows > Preferences > Validation 

Uncheck build for XML and XSL Validator's 

## Remove migration scripts from java build path

For some reason on import the sql migration scripts are added to the java build path, this results in the following build error:

Cannot nest 'geonetwork-main/src/main/webapp/WEB-INF/classes/setup/sql/migrate' inside library 'geonetwork-main/src/main/webapp/WEB-INF/classes'	geonetwork-main		line 1	Maven Java EE Configuration Problem

To fix this remove src/main/webapp/WEB-INF/classes/setup/sql/migrate from the java build path for the geonetwork-main project

Right click on the geonetwork-main project and select properties.

Select Java Build Path / Source 

Remove the src/main/webapp/WEB-INF/classes/setup/sql/migrate entry

## Run a maven build 

Run a maven clean package on the GeoNetwork project

Referesh the workspace when finished

## Adjust geonetwork-main project web deployment settings

Right click on the geonetwork-main project and select properties.

Select Project Facets and tick Dynamic Web Module

## Correct the web deployment assembly

Right click on the geonetwork-main project and select properties.

Select Deployment Assembly

Remove all existing entries

Add the target/geonetwork folder

Recheck this setting if you have problems starting tomcat below as for me some of the maven depenedencies are re-added (or not deleted) and had to be removed again 

## Create a tomcat server

Servers > Create new server > Apache > Tomcat V7

Select location of tomcat 7 install directory or use download and install create

## Deploy Geonetwork main to tomcat

Right click on Tomact 7 server and select Add/Remove option

Add geonetwork-main

## Running other client interfaces

There are four client interfaces included in version 2.10.x to run an interface other than the default 'classic' interface (xsl generated html) add a config override property to the servers startup parameters (Run > Run Configurations > Apache Tomcat > Tomcat 7 server > Arguments):

html5ui -> -Dgeonetwork.jeeves.configuration.overrides.file=${workspace_loc}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-html5ui.xml

widget (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file=${workspace_loc}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widget.xml

widgettab (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file=${workspace_loc}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widgettab.xml



