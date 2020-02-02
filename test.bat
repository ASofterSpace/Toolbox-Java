@echo off

rd /s /q config

java -classpath bin com.asofterspace.toolbox.selftest.AllTests

pause
