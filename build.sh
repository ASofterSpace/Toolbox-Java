#!/bin/bash

rm -rf bin

mkdir bin

cd src

find . -name "*.java" > sourcefiles.list

javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list

cd ..

read -p "Build executed successfully; press a key to continue with the self-tests..."

./test.sh
