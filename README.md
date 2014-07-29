<<<<<<< HEAD
ANZMEST
-------

ANZMEST is GeoNetwork 2.10.x with Australian and New Zealand Metadata Profiles and support.

When you clone ANZMEST select the 2.10.x branch eg:

git clone https://github.com/anzmest/core-geonetwork.git -b 2.10.x --recursive

With ANZMEST you get GeoNetwork (2.10.x) plus:

* ISO19115/19139 ANZLIC Profile version 1.1
* ISO19115/19139 Marine Community Profile versions 1.4 and 1.5-experimental
* SensorML OGC Discovery Profile for sensor platform metadata
* EML GBIF profile
* ANZMETA (the old ANZLIC metadata profile) version 1.3 (view only - no editing)
* ISO19115:1 2013 FDIS (for testing and exploration only)

You can view the config overrides that ANZMEST applies to GeoNetwork at:

https://github.com/anzmest/core-geonetwork/blob/2.10.x/web/src/main/webapp/WEB-INF/anzmest-config-overrides.xml

After you have cloned the repository, this file can be found at:

clone-directory-name/web/src/main/webapp/WEB-INF/anzmest-config-overrides.xml

You can review the differences between ANZMEST-2.10.x and GeoNetwork 2.10.x 
in the github interface at https://github.com/anzmest/core-geonetwork/compare/geonetwork:2.10.x...2.10.x

Features
--------

* Immediate search access to local and distributed geospatial catalogues
* Up- and downloading of data, graphics, documents, pdf files and any other content type
* An interactive Web Map Viewer to combine Web Map Services from distributed servers around the world
* Online editing of metadata with a powerful template system
* Scheduled harvesting and synchronization of metadata between distributed catalogs
* Support for OGC-CSW 2.0.2 ISO Profile, OAI-PMH, Z39.50 protocols
* Fine-grained access control with group and user management
* Multi-lingual user interface
=======
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

html5ui -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-html5ui.xml

widget (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widget.xml

widgettab (deprecated) -> -Dgeonetwork.jeeves.configuration.overrides.file={eclipse workspace}/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/geonetwork-main/WEB-INF/config-overrides-widgettab.xml



>>>>>>> 4976157... Changes to simplify build and deployment in eclipse
