@echo off
chcp 936 >nul
title Campus Community - Stop All

echo ============================================
echo        Campus Community - Stop All
echo ============================================
echo.

echo [1/2] Stopping frontend (node)...
taskkill /f /im node.exe >nul 2>&1
echo [OK] Frontend stopped

echo [2/2] Stopping backend (java)...
taskkill /f /fi "WINDOWTITLE eq Backend-8086*" >nul 2>&1
echo [OK] Backend stopped

echo.
echo ============================================
echo   All services stopped
echo ============================================
echo.
pause
