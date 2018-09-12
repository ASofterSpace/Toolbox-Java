#!/bin/bash

rm -rf bin

mkdir bin

cd src

find . -name "*.java" > sourcefiles.list

javac -d ../bin @sourcefiles.list

cd ..

read -p "Build executed successfully; press a key to continue with the self-tests..."

rm -rf config

java -classpath bin com.asofterspace.toolbox.selftest.AllTests
