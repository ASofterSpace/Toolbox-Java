#!/bin/bash

rm -rf bin

mkdir bin

cd src

find . -name "*.java" > sourcefiles.list

javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list

cd ..

rm -rf config

java -classpath bin com.asofterspace.toolbox.selftest.AllTests
