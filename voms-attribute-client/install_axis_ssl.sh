#!/usr/bin/env bash

wget http://axis-ssl.googlecode.com/files/axis-ssl-1.4.jar
mvn install:install-file -Dfile=axis-ssl-1.4.jar -DgroupId=org.apache.axis -DartifactId=axis-ssl -Dversion=1.4 -Dpackaging=jar -DgeneratePom=true
