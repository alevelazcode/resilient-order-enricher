#!/bin/bash

# IDE Formatting Setup Script for Resilient Order Enricher
# This script sets up automatic formatting on save for various IDEs

set -e

echo "🔧 Setting up automatic formatting on save..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to setup VS Code
setup_vscode() {
    if command_exists code; then
        echo -e "${BLUE}Setting up VS Code formatting...${NC}"

        # Create .vscode directory if it doesn't exist
        mkdir -p .vscode

        # Create settings.json for VS Code
        cat > .vscode/settings.json << 'EOF'
{
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.organizeImports": "explicit"
    },
    "java.format.enabled": true,
    "java.format.onType.enabled": true,
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
    "java.format.settings.profile": "GoogleStyle",
    "go.formatTool": "gofumpt",
    "go.lintTool": "golangci-lint",
    "go.lintOnSave": "package",
    "go.useLanguageServer": true,
    "go.toolsManagement.autoUpdate": true,
    "files.associations": {
        "*.gradle.kts": "kotlin",
        "*.yml": "yaml",
        "*.yaml": "yaml"
    },
    "yaml.format.enable": true,
    "yaml.validate": true,
    "json.format.enable": true,
    "markdown.format.enable": true,
    "prettier.requireConfig": false,
    "prettier.useEditorConfig": true
}
EOF

        # Create extensions.json for recommended extensions
        cat > .vscode/extensions.json << 'EOF'
{
    "recommendations": [
        "vscjava.vscode-java-pack",
        "redhat.java",
        "vscjava.vscode-gradle",
        "golang.go",
        "ms-vscode.vscode-json",
        "redhat.vscode-yaml",
        "esbenp.prettier-vscode",
        "ms-vscode.vscode-docker",
        "ms-azuretools.vscode-docker"
    ]
}
EOF

        echo -e "${GREEN}VS Code formatting configured!${NC}"
        echo -e "${YELLOW}Install recommended extensions with: code --install-extension <extension-id>${NC}"
    else
        echo -e "${YELLOW}VS Code not found. Install it to enable automatic formatting.${NC}"
    fi
}

# Function to setup IntelliJ IDEA
setup_intellij() {
    echo -e "${BLUE}Setting up IntelliJ IDEA formatting...${NC}"
    echo -e "${YELLOW}For IntelliJ IDEA, manually configure:${NC}"
    echo "1. Go to Settings/Preferences > Editor > Code Style"
    echo "2. Import the Google Style XML from:"
    echo "   https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml"
    echo "3. Enable 'Reformat code on save' in Settings/Preferences > Tools > Actions on Save"
    echo "4. Install Go plugin for Go formatting"
}

# Function to setup Eclipse
setup_eclipse() {
    echo -e "${BLUE}Setting up Eclipse formatting...${NC}"
    echo -e "${YELLOW}For Eclipse, manually configure:${NC}"
    echo "1. Install Google Style formatter"
    echo "2. Enable 'Format source code on save' in Window > Preferences > Java > Editor > Save Actions"
}

# Function to setup pre-commit hooks
setup_precommit() {
    echo -e "${BLUE}Setting up pre-commit hooks...${NC}"

    if command_exists pre-commit; then
        pre-commit install
        pre-commit install --hook-type pre-push
        echo -e "${GREEN}Pre-commit hooks installed!${NC}"
    else
        echo -e "${YELLOW}Installing pre-commit...${NC}"
        if command_exists pip; then
            pip install pre-commit
        elif command_exists pip3; then
            pip3 install pre-commit
        elif command_exists brew; then
            brew install pre-commit
        else
            echo -e "${YELLOW}Please install pre-commit manually: https://pre-commit.com/#install${NC}"
        fi

        if command_exists pre-commit; then
            pre-commit install
            pre-commit install --hook-type pre-push
            echo -e "${GREEN}Pre-commit hooks installed!${NC}"
        fi
    fi
}

# Function to setup Git hooks
setup_git_hooks() {
    echo -e "${BLUE}Setting up Git hooks...${NC}"

    # Create .git/hooks directory if it doesn't exist
    mkdir -p .git/hooks

    # Create pre-commit hook
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash

# Pre-commit hook for automatic formatting
echo "🔧 Running pre-commit checks..."

# Run Java formatting
if [ -f "./gradlew" ]; then
    echo "📝 Formatting Java code..."
    ./gradlew spotlessApply
fi

# Run Go formatting
if [ -d "services/enricher-api-go" ]; then
    echo "📝 Formatting Go code..."
    cd services/enricher-api-go && make format && cd ../..
fi

# Run pre-commit hooks if available
if command -v pre-commit >/dev/null 2>&1; then
    echo "🔍 Running pre-commit hooks..."
    pre-commit run --all-files
fi

echo "✅ Pre-commit checks completed!"
EOF

    # Make the hook executable
    chmod +x .git/hooks/pre-commit

    echo -e "${GREEN}Git hooks configured!${NC}"
}

# Function to setup editor config
setup_editorconfig() {
    echo -e "${BLUE}Setting up EditorConfig...${NC}"

    cat > .editorconfig << 'EOF'
# EditorConfig is awesome: https://EditorConfig.org

# top-most EditorConfig file
root = true

# Unix-style newlines with a newline ending every file
[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

# Java files
[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

# Go files
[*.go]
indent_style = tab
indent_size = 4

# YAML files
[*.{yml,yaml}]
indent_style = space
indent_size = 2

# JSON files
[*.json]
indent_style = space
indent_size = 2

# Markdown files
[*.md]
trim_trailing_whitespace = false
max_line_length = 80

# Gradle files
[*.{gradle,gradle.kts}]
indent_style = space
indent_size = 4

# Shell scripts
[*.sh]
indent_style = space
indent_size = 2

# Docker files
[Dockerfile]
indent_style = space
indent_size = 4
EOF

    echo -e "${GREEN}EditorConfig configured!${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}🚀 Setting up automatic formatting for Resilient Order Enricher${NC}"
    echo "=============================================="

    setup_editorconfig
    setup_vscode
    setup_intellij
    setup_eclipse
    setup_precommit
    setup_git_hooks

    echo ""
    echo -e "${GREEN}✅ Automatic formatting setup completed!${NC}"
    echo ""
    echo -e "${YELLOW}📋 Next steps:${NC}"
    echo "1. Restart your IDE to apply formatting settings"
    echo "2. Install recommended extensions for your IDE"
    echo "3. Test formatting by saving a file"
    echo "4. Run 'make format' to format all files manually"
    echo "5. Run 'make ci' to run all checks"

    echo ""
    echo -e "${BLUE}🎯 Now your code will be automatically formatted on save!${NC}"
}

# Run main function
main "$@"
