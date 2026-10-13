@echo off
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -STA -ExecutionPolicy RemoteSigned -File "%~dp0BluePointer-Spotlight.ps1"
if errorlevel 1 pause
