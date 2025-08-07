#!/bin/bash

# Prerequisites Check Script for Resilient Order Enricher
# This script verifies that all required tools are installed and working

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to detect OS
detect_os() {
    case "$(uname -s)" in
        Darwin*)    echo 'macos';;
        Linux*)     echo 'linux';;
        CYGWIN*|MINGW32*|MSYS*|MINGW*) echo 'windows';;
        *)          echo 'unknown';;
    esac
}

# Function to check if command exists and get version
check_command() {
    local cmd=$1
    local name=$2
    local min_version=$3

    if command -v "$cmd" >/dev/null 2>&1; then
        local version=$($cmd --version 2>/dev/null | head -n1)
        echo -e "${GREEN}✅ $name${NC}: $version"
        return 0
    else
        echo -e "${RED}❌ $name${NC}: Not installed"
        return 1
    fi
}

# Function to check Java version
check_java() {
    if command -v java >/dev/null 2>&1; then
        local version=$(java --version 2>/dev/null | head -n1 | awk '{print $2}' | cut -d'.' -f1)
        if [ "$version" -ge 21 ]; then
            echo -e "${GREEN}✅ Java${NC}: $(java --version | head -n1)"
            return 0
        else
            echo -e "${RED}❌ Java${NC}: Version $version found, but Java 21+ is required"
            return 1
        fi
    else
        echo -e "${RED}❌ Java${NC}: Not installed"
        return 1
    fi
}

# Function to check Go version
check_go() {
    if command -v go >/dev/null 2>&1; then
        local version=$(go version | awk '{print $3}' | sed 's/go//' | cut -d'.' -f2)
        if [ "$version" -ge 21 ]; then
            echo -e "${GREEN}✅ Go${NC}: $(go version)"
            return 0
        else
            echo -e "${RED}❌ Go${NC}: Version found, but Go 1.21+ is required"
            return 1
        fi
    else
        echo -e "${RED}❌ Go${NC}: Not installed"
        return 1
    fi
}

# Function to check Docker
check_docker() {
    if command -v docker >/dev/null 2>&1; then
        if docker info >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Docker${NC}: $(docker --version)"
            return 0
        else
            echo -e "${YELLOW}⚠️  Docker${NC}: Installed but not running. Start Docker Desktop or Docker daemon."
            return 1
        fi
    else
        echo -e "${RED}❌ Docker${NC}: Not installed"
        return 1
    fi
}

# Function to check Docker Compose
check_docker_compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Docker Compose${NC}: $(docker-compose --version)"
        return 0
    elif docker compose version >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Docker Compose${NC}: $(docker compose version)"
        return 0
    else
        echo -e "${RED}❌ Docker Compose${NC}: Not installed"
        return 1
    fi
}

# Function to check system resources
check_system_resources() {
    echo -e "${BLUE}📊 System Resources:${NC}"

    local os=$(detect_os)

    # Check RAM
    local ram_gb=0
    case $os in
        macos)
            ram_gb=$(sysctl -n hw.memsize | awk '{print int($1/1024/1024/1024)}')
            ;;
        linux)
            if command -v free >/dev/null 2>&1; then
                ram_gb=$(free -g | awk '/^Mem:/{print $2}')
            else
                ram_gb=$(awk '/MemTotal/ {print int($2/1024/1024)}' /proc/meminfo)
            fi
            ;;
        windows)
            # For Windows, we'll skip RAM check as it's complex in bash
            ram_gb=8  # Assume minimum
            ;;
        *)
            ram_gb=8  # Assume minimum for unknown OS
            ;;
    esac

    if [ "$ram_gb" -ge 8 ]; then
        echo -e "${GREEN}✅ RAM${NC}: ${ram_gb}GB available"
    else
        echo -e "${YELLOW}⚠️  RAM${NC}: ${ram_gb}GB available (8GB+ recommended)"
    fi

    # Check disk space
    local disk_gb=0
    case $os in
        macos)
            disk_gb=$(df -g . | awk 'NR==2{print $4}')
            ;;
        linux)
            if command -v df >/dev/null 2>&1; then
                disk_gb=$(df -BG . | awk 'NR==2{print $4}' | sed 's/G//')
            else
                disk_gb=10  # Assume minimum
            fi
            ;;
        windows)
            # For Windows, we'll skip disk check as it's complex in bash
            disk_gb=10  # Assume minimum
            ;;
        *)
            disk_gb=10  # Assume minimum for unknown OS
            ;;
    esac

    if [ "$disk_gb" -ge 10 ]; then
        echo -e "${GREEN}✅ Disk Space${NC}: ${disk_gb}GB available"
    else
        echo -e "${YELLOW}⚠️  Disk Space${NC}: ${disk_gb}GB available (10GB+ recommended)"
    fi
}

# Function to check ports
check_ports() {
    echo -e "${BLUE}🔌 Port Availability:${NC}"
    local ports=(8081 8090 9092 27017 6380)
    local all_available=true

    for port in "${ports[@]}"; do
        if lsof -i ":$port" >/dev/null 2>&1; then
            echo -e "${YELLOW}⚠️  Port $port${NC}: In use"
            all_available=false
        else
            echo -e "${GREEN}✅ Port $port${NC}: Available"
        fi
    done

    if [ "$all_available" = true ]; then
        return 0
    else
        return 1
    fi
}

# Main execution
main() {
    echo -e "${BLUE}🔍 Checking Prerequisites for Resilient Order Enricher${NC}"
    echo "=============================================="
    echo ""

    local all_good=true

    # Check required commands
    echo -e "${BLUE}📦 Required Software:${NC}"
    check_java || all_good=false
    check_go || all_good=false
    check_docker || all_good=false
    check_docker_compose || all_good=false
    check_command git "Git" || all_good=false
    check_command make "Make" || all_good=false

    echo ""
    check_system_resources
    echo ""
    check_ports

    echo ""
    echo "=============================================="

    if [ "$all_good" = true ]; then
        echo -e "${GREEN}🎉 All prerequisites are met! You're ready to run the project.${NC}"
        echo ""
        echo -e "${BLUE}Next steps:${NC}"
        echo "1. Clone the repository: git clone <your-repo-url>"
        echo "2. Setup formatting: make setup-formatting"
        echo "3. Start services: make start"
        echo "4. Run tests: make test"
    else
        echo -e "${RED}❌ Some prerequisites are missing. Please install them before proceeding.${NC}"
        echo ""
        echo -e "${BLUE}Installation guides:${NC}"
        echo "📖 See the Technical Prerequisites section in README.md"
        echo "🔗 Or run: make setup-formatting (after installing basic tools)"
    fi

    echo ""
}

# Run main function
main "$@"
