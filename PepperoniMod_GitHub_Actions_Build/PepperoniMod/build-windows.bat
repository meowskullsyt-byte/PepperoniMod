@echo off
setlocal
cd /d "%~dp0"
echo Pepperoni Mod - Minecraft Forge 1.20.1 build
where java >nul 2>nul || (echo ERROR: Install a Java 17 JDK first. & pause & exit /b 1)
where gradle >nul 2>nul || (echo ERROR: Gradle is not installed or not on PATH. Install Gradle 8.1.1 or newer, then reopen this window. & pause & exit /b 1)
echo.
gradle build
if errorlevel 1 (
  echo.
  echo BUILD FAILED. Copy the error above when asking for help.
  pause
  exit /b 1
)
echo.
echo BUILD COMPLETE. Your mod JAR should be in:
echo %CD%\build\libs\pepperoni-1.0.0.jar
pause
