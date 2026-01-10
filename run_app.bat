@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
echo Using JAVA_HOME as %JAVA_HOME%
call .\mvnw javafx:run
if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code %errorlevel%.
)
pause
