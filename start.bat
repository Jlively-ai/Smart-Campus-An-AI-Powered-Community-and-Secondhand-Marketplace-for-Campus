@echo off
chcp 936 >nul
title Campus Community - Start All

echo ============================================
echo        Campus Community - Start All
echo ============================================
echo.

set PROJECT_ROOT=%~dp0
set BACKEND=%PROJECT_ROOT%stucompla-rear2-feature-v0.0.1
set FRONTEND=%PROJECT_ROOT%stucompla-front-feature-v0.0.1
set ADMIN=%PROJECT_ROOT%admin\stucompla-front-admin-feature-v0.0.1

echo [1/3] Checking environment...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found
    pause
    exit /b 1
)
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js not found
    pause
    exit /b 1
)
echo [OK] Environment check passed
echo.

echo [2/3] Starting backend (Spring Boot)...
start "Backend-8086" cmd /k "cd /d %BACKEND% && mvn spring-boot:run"
echo [OK] Backend starting on port 8086
echo.

echo Waiting for backend to start (15s)...
timeout /t 15 /nobreak >nul

echo [3/3] Starting frontend services...
start "StudentFront-8080" cmd /k "cd /d %FRONTEND% && npm run dev"
echo [OK] Student frontend starting on port 8080

start "AdminFront-9528" cmd /k "cd /d %ADMIN% && npm run dev"
echo [OK] Admin frontend starting on port 9528

echo.
echo ============================================
echo   All services started!
echo.
echo   Backend:       http://localhost:8086
echo   Student Front: http://localhost:8080
echo   Admin Panel:   http://localhost:9528
echo.
echo   Close this window will NOT stop services
echo   To stop, close the corresponding windows
echo ============================================
echo.
pause
