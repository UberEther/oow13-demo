#!/bin/bash

mvn install:install-file -Dfile=circuit.jar -DgroupId=org.uberether.oow.oag -DartifactId=circuit -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=common.jar -DgroupId=org.uberether.oow.oag -DartifactId=common -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=entityStore.jar -DgroupId=org.uberether.oow.oag -DartifactId=entityStore -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=precipitate.jar -DgroupId=org.uberether.oow.oag -DartifactId=precipitate -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=manager.jar -DgroupId=org.uberether.oow.oag -DartifactId=manager -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=client.jar -DgroupId=org.uberether.oow.oag -DartifactId=client -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=server.jar -DgroupId=org.uberether.oow.oag -DartifactId=server -Dversion=7.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=com.vordel.circuit.oauth.jar -DgroupId=org.uberether.oow.oag -DartifactId=com.vordel.circuit.oauth -Dversion=7.1.1 -Dpackaging=jar
