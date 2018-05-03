md bin

cd src

dir /s /B *.java > sourcefiles.list

javac -d ../bin @sourcefiles.list

pause

cd ..

java -classpath bin com.asofterspace.toolbox.selftest.AllTests

pause
