@echo off

rd /s /q bin 2>nul

md bin

cd src

dir /s /B *.java > sourcefiles.list

javac -deprecation -Xlint:all -encoding utf8 -d ../bin @sourcefiles.list

cd ..

echo "Build executed successfully; press [Enter] to continue with the self-tests..."

pause

test.bat
