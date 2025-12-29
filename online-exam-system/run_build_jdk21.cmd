@echo off
set "JAVA_HOME=C:\Users\16673\apps\java\jdk-21.0.9"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
echo Using JAVA_HOME=%JAVA_HOME%
mvnw.cmd package
exit /b %ERRORLEVEL%
