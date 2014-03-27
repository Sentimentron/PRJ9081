#!/bin/bash

# Installs custom ark-tweet-nlp JAR

mvn install:install-file -Dfile=./ark-tweet-nlp-0.3.3.jar -DgroupId=edu.cmu.cs -DartifactId=ark-tweet-nlp \
    -Dversion=0.3.3 -Dpackaging=jar
