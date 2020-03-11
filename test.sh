#!/bin/bash

rm -rf config

java -classpath bin com.asofterspace.toolbox.selftest.AllTests
