#!/bin/bash
# Create virtual serial port that connects to MCAN TCP Bridge server
#
# This helper script creates a virtual serial port for use with the MCAN Protocol
# Serial To TCP Multiplexer Bridge, allowing serial-only software (e.g., programmer
# tools) to work alongside JMRI by sharing the same CC-Schnitte adapter.
#
# Author: Ralf Lang (ralf.lang@ralf-lang.de)
# Copyright: (C) 2026 JMRI Community
#
# Usage: ./create-mcan-virtual-port.sh <virtual_port> <tcp_host> <tcp_port>
#
# Example: ./create-mcan-virtual-port.sh /dev/ttyVCOM0 localhost 15731

set -e

if [ $# -ne 3 ]; then
    echo "Usage: $0 <virtual_port> <tcp_host> <tcp_port>"
    echo ""
    echo "Example: $0 /dev/ttyVCOM0 localhost 15731"
    echo ""
    echo "This creates a virtual serial port that connects to the bridge's TCP server."
    echo "Use the virtual port in your programmer software."
    exit 1
fi

VIRTUAL_PORT="$1"
TCP_HOST="$2"
TCP_PORT="$3"

# Check if socat is installed
if ! command -v socat &> /dev/null; then
    echo "Error: socat is not installed"
    echo ""
    echo "Install with:"
    echo "  Debian/Ubuntu: sudo apt-get install socat"
    echo "  Fedora/RHEL:   sudo dnf install socat"
    echo "  macOS:         brew install socat"
    echo "  Arch Linux:    sudo pacman -S socat"
    exit 1
fi

echo "Creating virtual serial port..."
echo "  Virtual port: $VIRTUAL_PORT"
echo "  TCP server:   $TCP_HOST:$TCP_PORT"
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Create PTY linked to TCP connection
# Options:
#   PTY,link=...  - Create pseudo-terminal at specified path
#   raw           - Raw mode (no line buffering)
#   echo=0        - Disable echo
#   TCP:...       - Connect to TCP server
socat PTY,link="$VIRTUAL_PORT",raw,echo=0 TCP:"$TCP_HOST":"$TCP_PORT"
