@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%\start_all.ps1" %*
if errorlevel 1 (
    echo.
    echo FlexFit launcher encountered an error.
    pause
)
endlocal
