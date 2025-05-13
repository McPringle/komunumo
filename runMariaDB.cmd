@echo off
setlocal

:: Check if podman is available
where podman >nul 2>nul
if %errorlevel%==0 (
    set "CMD=podman"
) else (
    :: Check if docker is available
    where docker >nul 2>nul
    if %errorlevel%==0 (
        set "CMD=docker"
    ) else (
        echo Error: Neither 'podman' nor 'docker' is installed or available in PATH.
        exit /b 1
    )
)

:: Run the compose commands
%CMD% compose up -d
if %errorlevel%==0 (
    %CMD% compose logs -f
) else (
    %CMD% compose down
)

endlocal
