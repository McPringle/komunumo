$cmd = ""

# Check which container tool is available
if (Get-Command podman -ErrorAction SilentlyContinue) {
    $cmd = "podman"
} elseif (Get-Command docker -ErrorAction SilentlyContinue) {
    $cmd = "docker"
} else {
    Write-Error "Neither 'podman' nor 'docker' is installed or available in PATH."
    exit 1
}

# Guard variable to ensure cleanup only runs once
$script:cleaned_up = $false

# Ensure compose down is run on exit (e.g. Ctrl+C or script exit)
function Cleanup {
    if (-not $script:cleaned_up) {
        Write-Host "Cleaning up..."
        & $cmd compose down
        $script:cleaned_up = $true
    }
}

# Register handlers for Ctrl+C and script exit
Register-EngineEvent PowerShell.Exiting -Action { Cleanup } | Out-Null
Register-EngineEvent Console.ControlC -Action { Cleanup; exit } | Out-Null

# Main execution
& $cmd compose up -d
& $cmd compose logs -f

# Fallback cleanup (if logs -f ends normally)
Cleanup
