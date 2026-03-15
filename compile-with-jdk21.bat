@echo off
set JAVA_HOME=D:\software\IntelliJ IDEA 2025.1\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
java -version
echo.
echo Running Maven compile...
mvn clean compile -DskipTests
