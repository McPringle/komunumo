#!/usr/bin/env pwsh

param(
    [switch]$Help
)

if ($Help) {
    Write-Host "Komunumo Development Services Runner for Windows" -ForegroundColor Green
    Write-Host ""
    Write-Host "This script starts the development services defined in docker-compose.yml:"
    Write-Host "  - MariaDB database"
    Write-Host "  - Adminer (database web interface)"
    Write-Host "  - MailPit (local mail server)"
    Write-Host ""
    Write-Host "Usage: .\runServices.ps1"
    Write-Host "       .\runServices.ps1 -Help"
    Write-Host ""
    Write-Host "Press Ctrl+C to stop all services and exit."
    exit 0
}

$cmd = ""

Write-Host "Checking for container runtime..." -ForegroundColor Yellow

if (Get-Command podman -ErrorAction SilentlyContinue) {
    $cmd = "podman"
    Write-Host "Found Podman" -ForegroundColor Green
} elseif (Get-Command docker -ErrorAction SilentlyContinue) {
    $cmd = "docker"
    Write-Host "Found Docker" -ForegroundColor Green
} else {
    Write-Host "Error: Neither 'podman' nor 'docker' is installed or available in PATH." -ForegroundColor Red
    Write-Host "Please install Docker Desktop or Podman and ensure it's in your PATH." -ForegroundColor Red
    exit 1
}

Write-Host "Testing $cmd availability..." -ForegroundColor Yellow
$testResult = & $cmd version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: $cmd is not running or not accessible." -ForegroundColor Red
    if ($cmd -eq "docker") {
        Write-Host "Please start Docker Desktop and try again." -ForegroundColor Red
    }
    exit 1
}

$script:cleaned_up = $false

function Cleanup {
    if (-not $script:cleaned_up) {
        Write-Host ""
        Write-Host "Shutting down services..." -ForegroundColor Yellow
        try {
            & $cmd compose down
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Services stopped successfully." -ForegroundColor Green
            } else {
                Write-Host "Warning: Some services may not have stopped cleanly." -ForegroundColor Yellow
            }
        } catch {
            Write-Host "Error during cleanup: $_" -ForegroundColor Red
        }
        $script:cleaned_up = $true
    }
}

$null = Register-EngineEvent PowerShell.Exiting -Action {
    Cleanup
}

try {
    Write-Host "Starting services with $cmd compose..." -ForegroundColor Yellow
    
    $result = & $cmd compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to start services." -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Services started successfully!" -ForegroundColor Green
    Write-Host "Available services:" -ForegroundColor Cyan
    Write-Host "  - MariaDB: localhost:3306" -ForegroundColor Cyan
    Write-Host "  - Adminer: http://localhost:4000" -ForegroundColor Cyan
    Write-Host "  - MailPit: http://localhost:8025" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Press Ctrl+C to stop all services and exit." -ForegroundColor Yellow
    Write-Host "Following logs..." -ForegroundColor Yellow
    Write-Host ""
    
    & $cmd compose logs -f
    
} catch [System.OperationCanceledException] {
    Write-Host "`nReceived interrupt signal..." -ForegroundColor Yellow
} catch {
    Write-Host "An error occurred: $_" -ForegroundColor Red
} finally {
    Cleanup
}
