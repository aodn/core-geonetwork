Eclipse Setup
-------------

## Install Eclipse Luna (J2EE package)

## Import geonetwork clone

File > Import > Maven > Existing Maven Project

Select location of geonetwork workspace

yui-compressor - resolve later 

This can be fixed by changing the pom refer 

http://stackoverflow.com/questions/6008942/yuicompressor-plugin-execution-not-covered-in-m2e

this is an eclipse specific fix which I'm not sure is appropriate for core

wait for build to finish or cancel as we are changing more build options below

## servlet-api

servlet-api is provided by containers and shouldn't be included in geonetwork-main generated war (causes problems when deploying geonetwork to tomcat).  Its currently included transitively by the yammer metrics servlet dependency in geonetwork-main and the jeeves dependency

this can be fixed by overriding the scope of this transitive dependency by specifying the dependency explicitly (unfortunately we are forced by maven to specify the version as well) e.g. 

```
      <dependency>
          <groupId>javax.servlet</groupId>
          <artifactId>servlet-api</artifactId>
          <scope>provided</scope>
          <version>2.5</version>
      </dependency>
```

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

## Adjust geonetwork-main project web deployment settings

Right click on the geonetwork-main project and select properties.

Select Project Facets and tick Dynamic Web Module

## Include missing dependencies

Right click on the geonetwork-main project and select properties.

Select Deployment Assembly

Add > Java Build Path Entries > Maven Dependencies

Add > Folder > target/m2e-wtp/web-resources

## Fix geonetwork-client dependency as required

For some reason my geonetwork-client dependency wasn't being included in the generated web app (widget and htmlui interfaces would not work)

I had to delete this dependency and re-add it (project dependency)

## Build geonetwork project

Right click on geonetwork project and select Run As > Maven Build

Enter clean package in the maven goal and run.


## Create a tomcat server

Servers > Create new server > Apache > Tomcat V7

Select location of tomcat 7 install directory or use download and install create

## Deploy Geonetwork main to tomcat

Right click on Tomact 7 server and select Add/Remove option

Add geonetwork-main

## Running other client interfaces

There are four client interfaces included in version 2.10.x to run an interface other than the default 'classic' interface (xsl generated html) add a config override property to the servers startup parameters (Run > Run Configurations > Apache Tomcat > Tomcat 7 server > Arguments):

html5ui -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-html5ui.xml

widget (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widget.xml

widgettab (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widgettab.xml



