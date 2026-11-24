@echo off
setlocal
REM ============================================================
REM  SysTurismo - Genera el INSTALADOR para Windows (SysTurismo-1.0.0.exe)
REM  Requisitos (solo en la PC donde se genera, NO en las PC de los usuarios):
REM    1) JDK 21 (con jpackage)  ->  https://adoptium.net  (Temurin 21)
REM    2) WiX Toolset 3.x        ->  https://github.com/wixtoolset/wix3/releases
REM       (instalar y reiniciar la terminal para que candle.exe este en el PATH)
REM ============================================================
cd /d "%~dp0"

where jpackage >nul 2>nul || (echo [ERROR] No se encontro jpackage. Instale JDK 21 y agregue su carpeta bin al PATH. & exit /b 1)
where candle >nul 2>nul || (echo [ERROR] No se encontro WiX Toolset 3.x ^(candle.exe^). Instalelo y reinicie la terminal. & exit /b 1)

echo [1/3] Compilando el proyecto...
call mvnw.cmd clean package -DskipTests || (echo [ERROR] Fallo la compilacion. & exit /b 1)

echo [2/3] Preparando archivos...
if exist jpackage-input rmdir /s /q jpackage-input
if exist installer rmdir /s /q installer
mkdir jpackage-input
copy target\SysTurismo-1.0.0-jar-with-dependencies.jar jpackage-input\ >nul || (echo [ERROR] No se encontro el JAR. & exit /b 1)

echo [3/3] Generando instalador .exe ...
jpackage --type exe ^
  --name SysTurismo ^
  --app-version 1.0.0 ^
  --vendor "Universidad Peruana Union" ^
  --description "SysTurismo - Sistema de Agencia de Turismo" ^
  --icon packaging\SysTurismo.ico ^
  --input jpackage-input ^
  --main-jar SysTurismo-1.0.0-jar-with-dependencies.jar ^
  --main-class pe.edu.upeu.systurismo.Launcher ^
  --add-modules java.base,java.sql,java.naming,java.management,java.logging,java.desktop,jdk.unsupported,jdk.xml.dom ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-menu --win-menu-group SysTurismo ^
  --win-shortcut --win-dir-chooser ^
  --win-upgrade-uuid 6f1c2a54-8d3b-4e7a-9c15-3b2a7d9e41aa ^
  --dest installer || (echo [ERROR] Fallo jpackage. & exit /b 1)

echo.
echo ============================================================
echo  LISTO: installer\SysTurismo-1.0.0.exe
echo  Copie ese unico archivo a las PC con Windows 10/11 (64 bits)
echo  y ejecutelo con doble clic. No requiere instalar Java.
echo  Los datos se guardan en %%USERPROFILE%%\SysTurismo\data
echo ============================================================
endlocal
