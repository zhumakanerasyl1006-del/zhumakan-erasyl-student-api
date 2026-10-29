#!/usr/bin/env bash
set -euo pipefail

# Resolve paths relative to this script, independently of the caller's directory.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
LOG_FILE="$PROJECT_DIR/logs/Zhumakan_Erasyl_system_report.log"
mkdir -p -- "$PROJECT_DIR/logs"

{
    printf '\n=== System report ===\n'
    printf 'Full name: Zhumakan Erasyl\n'
    printf 'Current date and time: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %Z (%z)')"
    printf 'Current Linux username: %s\n' "$(id -un)"
    printf '\n--- Operating system ---\n'
    cat /etc/os-release
    printf 'Kernel version: %s\n' "$(uname -r)"
    printf '\n--- CPU information ---\n'
    lscpu
    printf '\n--- Memory information ---\n'
    free -h
    printf '\n--- Disk usage ---\n'
    df -h
    printf '\n--- Available disk space (project filesystem) ---\n'
    df -h --output=source,size,used,avail,pcent,target -- "$PROJECT_DIR"
    printf '\n=== End of report ===\n'
} 2>&1 | tee -a -- "$LOG_FILE"
