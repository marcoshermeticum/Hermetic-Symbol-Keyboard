@echo off
echo ============================================
echo  Hermetic Symbol Keyboard - Setup Script
echo ============================================
echo.
echo This script downloads the Gradle Wrapper JAR needed to build the project.
echo Alternatively, just open the project in Android Studio and it will handle this automatically.
echo.

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle wrapper already exists. Nothing to do.
    goto end
)

echo Downloading Gradle Wrapper JAR...
powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.5-bin.zip' -OutFile 'gradle-8.5-bin.zip'"
echo.
echo Downloaded. Please extract gradle-8.5-bin.zip and run:
echo   gradle-8.5\bin\gradle.bat wrapper
echo.
echo Or simply open the project in Android Studio, which will configure the wrapper automatically.
echo.

:end
echo.
echo Setup complete. Next steps:
echo   1. Open this project in Android Studio
echo   2. Wait for Gradle sync to complete
echo   3. Build ^> Make Project (Ctrl+F9)
echo   4. Run on device (Shift+F10)
echo.
pause
