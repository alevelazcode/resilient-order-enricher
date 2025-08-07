#!/bin/bash

# Install Code Quality Tools Script
# =================================

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Installing Code Quality Tools...${NC}"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to install Go tools
install_go_tools() {
    echo -e "${BLUE}Installing Go tools...${NC}"

    # Check if Go is installed
    if ! command_exists go; then
        echo -e "${RED}Go is not installed. Please install Go first.${NC}"
        exit 1
    fi

    # Install goimports
    if ! command_exists goimports; then
        echo -e "${YELLOW}Installing goimports...${NC}"
        go install golang.org/x/tools/cmd/goimports@latest
    else
        echo -e "${GREEN}goimports already installed${NC}"
    fi

    # Install gofumpt
    if ! command_exists gofumpt; then
        echo -e "${YELLOW}Installing gofumpt...${NC}"
        go install mvdan.cc/gofumpt@latest
    else
        echo -e "${GREEN}gofumpt already installed${NC}"
    fi

    # Install golangci-lint
    if ! command_exists golangci-lint; then
        echo -e "${YELLOW}Installing golangci-lint...${NC}"
        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b $(go env GOPATH)/bin v1.55.2
    else
        echo -e "${GREEN}golangci-lint already installed${NC}"
    fi
}

# Function to install pre-commit
install_pre_commit() {
    echo -e "${BLUE}Installing pre-commit...${NC}"

    if command_exists pre-commit; then
        echo -e "${GREEN}pre-commit already installed${NC}"
        return 0
    fi

    # Try different installation methods
    if command_exists pip3; then
        echo -e "${YELLOW}Installing pre-commit with pip3...${NC}"
        pip3 install pre-commit
    elif command_exists pip; then
        echo -e "${YELLOW}Installing pre-commit with pip...${NC}"
        pip install pre-commit
    elif command_exists brew; then
        echo -e "${YELLOW}Installing pre-commit with Homebrew...${NC}"
        brew install pre-commit
    elif command_exists apt-get; then
        echo -e "${YELLOW}Installing pre-commit with apt...${NC}"
        sudo apt-get update && sudo apt-get install -y python3-pip
        pip3 install pre-commit
    elif command_exists yum; then
        echo -e "${YELLOW}Installing pre-commit with yum...${NC}"
        sudo yum install -y python3-pip
        pip3 install pre-commit
    else
        echo -e "${RED}Cannot install pre-commit. Please install it manually:${NC}"
        echo "  pip install pre-commit"
        echo "  or visit: https://pre-commit.com/#installation"
        return 1
    fi
}

# Function to install Node.js tools (for prettier)
install_node_tools() {
    echo -e "${BLUE}Installing Node.js tools...${NC}"

    if ! command_exists node; then
        echo -e "${YELLOW}Node.js not found. Prettier formatting may not work.${NC}"
        echo -e "${YELLOW}Consider installing Node.js for better formatting support.${NC}"
        return 0
    fi

    if ! command_exists prettier; then
        echo -e "${YELLOW}Installing prettier globally...${NC}"
        npm install -g prettier
    else
        echo -e "${GREEN}prettier already installed${NC}"
    fi
}

# Function to setup pre-commit hooks
setup_pre_commit() {
    echo -e "${BLUE}Setting up pre-commit hooks...${NC}"

    if command_exists pre-commit; then
        pre-commit install
        echo -e "${GREEN}Pre-commit hooks installed${NC}"
    else
        echo -e "${RED}pre-commit not available. Hooks not installed.${NC}"
    fi
}

# Function to verify installation
verify_installation() {
    echo -e "${BLUE}Verifying tool installation...${NC}"
    echo ""

    # Check Go tools
    echo -e "${YELLOW}Go Tools:${NC}"
    if command_exists go; then
        echo -e "  ✓ go: $(go version | cut -d' ' -f3)"
    else
        echo -e "  ✗ go: not installed"
    fi

    if command_exists goimports; then
        echo -e "  ✓ goimports: installed"
    else
        echo -e "  ✗ goimports: not installed"
    fi

    if command_exists gofumpt; then
        echo -e "  ✓ gofumpt: installed"
    else
        echo -e "  ✗ gofumpt: not installed"
    fi

    if command_exists golangci-lint; then
        echo -e "  ✓ golangci-lint: $(golangci-lint version --format short 2>/dev/null || echo 'installed')"
    else
        echo -e "  ✗ golangci-lint: not installed"
    fi

    echo ""

    # Check Java tools (Gradle handles these)
    echo -e "${YELLOW}Java Tools:${NC}"
    if command_exists java; then
        echo -e "  ✓ java: $(java -version 2>&1 | head -n 1 | cut -d'"' -f2)"
    else
        echo -e "  ✗ java: not installed"
    fi

    if [ -f "./gradlew" ]; then
        echo -e "  ✓ gradle: available via gradlew"
    else
        echo -e "  ✗ gradle: gradlew not found"
    fi

    echo ""

    # Check other tools
    echo -e "${YELLOW}Other Tools:${NC}"
    if command_exists pre-commit; then
        echo -e "  ✓ pre-commit: $(pre-commit --version | cut -d' ' -f2)"
    else
        echo -e "  ✗ pre-commit: not installed"
    fi

    if command_exists prettier; then
        echo -e "  ✓ prettier: $(prettier --version)"
    else
        echo -e "  ✗ prettier: not installed"
    fi

    if command_exists docker; then
        echo -e "  ✓ docker: $(docker --version | cut -d' ' -f3 | tr -d ',')"
    else
        echo -e "  ✗ docker: not installed"
    fi

    echo ""
}

# Main installation process
main() {
    echo -e "${BLUE}=== Code Quality Tools Installation ===${NC}"
    echo ""

    # Install tools
    install_go_tools
    echo ""

    install_pre_commit
    echo ""

    install_node_tools
    echo ""

    setup_pre_commit
    echo ""

    verify_installation

    echo -e "${GREEN}=== Installation Complete! ===${NC}"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "  1. Run 'make format' to format all code"
    echo "  2. Run 'make lint' to check code quality"
    echo "  3. Run 'make pre-commit' for full pre-commit checks"
    echo "  4. Run 'make quality-report' to generate a quality report"
    echo ""
    echo -e "${BLUE}Available commands:${NC}"
    echo "  make help              # Show all available commands"
    echo "  make format            # Format all code"
    echo "  make lint              # Lint all code"
    echo "  make quality-check     # Run comprehensive quality checks"
    echo "  make pre-commit        # Run pre-commit pipeline"
    echo ""
}

# Run main function
main "$@"
