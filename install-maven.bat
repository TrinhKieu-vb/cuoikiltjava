@echo off
REM Request admin rights and run PowerShell script
PowerShell -Command "Start-Process PowerShell -ArgumentList '-NoProfile -ExecutionPolicy Bypass -File \"%~dp0install-maven.ps1\"' -Verb RunAs"
pause

