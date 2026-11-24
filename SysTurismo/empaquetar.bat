@echo off
REM Genera la aplicación Windows (SysTurismo.exe). Requiere JDK 21 con jpackage.
REM Para un instalador .exe/.msi además hay que instalar WiX Toolset y cambiar --type app-image por --type exe.
call mvnw.cmd clean package -DskipTests || exit /b 1
if exist dist rmdir /s /q dist
if exist jpackage-input rmdir /s /q jpackage-input
mkdir jpackage-input
copy target\SysTurismo-1.0.0-jar-with-dependencies.jar jpackage-input\ >nul
jpackage --type app-image --name SysTurismo --input jpackage-input --main-jar SysTurismo-1.0.0-jar-with-dependencies.jar --main-class pe.edu.upeu.systurismo.Launcher --dest dist || exit /b 1
echo Listo: dist\SysTurismo\SysTurismo.exe
echo Los datos se guardan en %USERPROFILE%\SysTurismo\data
