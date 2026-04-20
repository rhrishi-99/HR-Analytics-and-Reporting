@echo off
setlocal

set JAVAC="C:\Program Files\Java\jdk-25.0.2\bin\javac.exe"
set JAVA="C:\Program Files\Java\jdk-25.0.2\bin\java.exe"
set SRC=src\main\java
set RES=src\main\resources
set OUT=target\classes

echo === HR Analytics: Compiling ===
if not exist %OUT% mkdir %OUT%

:: Copy resources (HTML, etc.) to classpath root
if exist %RES% xcopy /s /q /y %RES%\* %OUT%\ >nul 2>&1

:: Collect all .java files into a temp list
dir /s /b %SRC%\*.java > .sources.txt

%JAVAC% --release 17 -d %OUT% @.sources.txt
if errorlevel 1 (
    echo [FAIL] Compilation failed.
    del .sources.txt
    exit /b 1
)
del .sources.txt

if "%1"=="web" (
    echo === HR Analytics: Starting Web Server ===
    echo    Open http://localhost:8080 in your browser
    echo    Press Ctrl+C to stop
    echo.
    %JAVA% -cp %OUT% com.hranalytics.web.WebMain
) else (
    echo === HR Analytics: Running Console Demo ===
    %JAVA% -cp %OUT% com.hranalytics.Main
)

endlocal
