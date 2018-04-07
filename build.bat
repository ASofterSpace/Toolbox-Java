md bin

cd src

dir /s /B *.java > sourcefiles.list

javac -d ../bin @sourcefiles.list

pause
