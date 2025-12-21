#!/bin/bash

# --- Настройка и окружение ---
set -e
cd "$(dirname "$0")"

# --- Обработка аргументов ---
IS_PRERELEASE=false

show_help() {
    echo "Usage: $0 [arguments]"
    echo ""
    echo "Arguments:"
    echo "  --pre    Enable pre-release"
    echo "  --help   Show this message"
}

for arg in "$@"; do
    case $arg in
        --pre)
            IS_PRERELEASE=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo -e "\033[0;31mError: Unknown argument '$arg'\033[0m"
            show_help
            exit 1
            ;;
    esac
done

# --- Конфигурация цветов и оформления ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# --- Функции логирования ---
info() { echo -e "${BLUE}ℹ${NC} $1"; }
success() { echo -e "${GREEN}✓${NC} $1"; }
warning() { echo -e "${YELLOW}⚠${NC} $1"; }
error() { echo -e "${RED}✗${NC} $1"; }
step() { echo -e "\n${CYAN}${BOLD}→${NC} ${BOLD}$1${NC}"; }
substep() { echo -e "  ${GREEN}•${NC} $1"; }

echo -e "${MAGENTA}${BOLD}🚀 FromChat KMP Release Pipeline${NC}"
[[ "$IS_PRERELEASE" == true ]] && info "Prerelease mode enabled via --pre flag"

# --- Настройка памяти для предотвращения OutOfMemory ---
export GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx8g -Dkotlin.daemon.jvm.options=-Xmx8g"

# --- Переменные Git / Версии ---
step "Metadata collection"
TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.1")
# shellcheck disable=SC2207
TAGS=($(git --no-pager tag --sort -v:refname | xargs))
PREVTAG=${TAGS[1]:-$TAG}

substep "Current tag: ${YELLOW}$TAG${NC}"
substep "Previous tag: ${YELLOW}$PREVTAG${NC}"

BUILD_NUMBER=$(echo "$TAG" | sed 's/[^0-9]//g')
[[ -z "$BUILD_NUMBER" ]] && BUILD_NUMBER=1

# --- Сборка Android ---
build_android() {
    step "Building Android Release"
    if ./gradlew :app:android:assembleRelease -PbuildNumber="$BUILD_NUMBER" -Dorg.gradle.jvmargs="-Xmx8g"; then
        APK_SRC=$(find . -name "*release.apk" | head -n 1)

        if [[ -f "$APK_SRC" ]]; then
            mkdir -p releases
            DISPLAY_NAME="FromChat-$TAG-android.apk"
            cp "$APK_SRC" "releases/$DISPLAY_NAME"
            ANDROID_ASSET="releases/$DISPLAY_NAME"
            success "Android APK ready: ${CYAN}$DISPLAY_NAME${NC}"
        else
            error "Android APK not found after build"
            exit 1
        fi
    else
        error "Android build failed"
        exit 1
    fi
}

# --- Сборка iOS ---
build_ios() {
    step "Building iOS Release"
    if [[ "$OSTYPE" != "darwin"* ]]; then
        warning "iOS build requires macOS. Skipping."
        return
    fi

    export DEVELOPER_DIR="/Applications/Xcode.app/Contents/Developer"
    [[ ! -d "$DEVELOPER_DIR" ]] && export DEVELOPER_DIR=$(xcode-select -p)

    IOS_PROJECT_DIR="app/ios"
    [[ ! -d "$IOS_PROJECT_DIR" ]] && IOS_PROJECT_DIR="iosApp"

    substep "Setting iOS version to $BUILD_NUMBER..."
    PLIST_PATH=$(find "$IOS_PROJECT_DIR" -name "Info.plist" | head -n 1)
    if [[ -f "$PLIST_PATH" ]]; then
        /usr/libexec/PlistBuddy -c "Set :CFBundleVersion $BUILD_NUMBER" "$PLIST_PATH" || true
        /usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString ${TAG#v}" "$PLIST_PATH" || true
    fi

    substep "Xcode Archiving (Unsigned)..."
    if xcodebuild -project "$IOS_PROJECT_DIR/iosApp.xcodeproj" \
        -scheme iOS \
        -configuration Release \
        -sdk iphoneos \
        -destination 'generic/platform=iOS' \
        -derivedDataPath build-output \
        CODE_SIGNING_ALLOWED=NO \
        CODE_SIGNING_REQUIRED=NO \
        CODE_SIGNING_ENTITLEMENTS="" \
        clean build > ios_build.log 2>&1; then

        success "Xcode build successful."
    else
        error "Xcode build failed. Check ios_build.log"
        exit 1
    fi

    substep "Packaging IPA for TrollStore..."
    mkdir -p Payload
    APP_PATH=$(find build-output -name "*.app" -type d | head -n 1)
    if [[ -n "$APP_PATH" ]]; then
        cp -r "$APP_PATH" Payload/
        IPA_NAME="FromChat-$TAG-ios-unsigned.ipa"
        zip -r "releases/$IPA_NAME" Payload > /dev/null
        rm -rf Payload
        IOS_ASSET="releases/$IPA_NAME"
        success "iOS IPA ready: ${CYAN}$IPA_NAME${NC}"
    else
        error "Could not find .app bundle"
        exit 1
    fi
}

# --- Публикация в GitHub ---
publish_github() {
    step "GitHub Deployment"
    if ! command -v gh &> /dev/null; then
        warning "GitHub CLI (gh) not found. Skipping upload."
        return
    fi

    substep "Pushing tags..."
    git push --tags > /dev/null 2>&1 || true

    # Определяем флаг пререлиза на основе тега ИЛИ аргумента --pre
    prerelease_flag=""
    if [[ "$IS_PRERELEASE" == true ]] || [[ "$TAG" == v*-pre* ]]; then
        prerelease_flag="--prerelease"
    fi

    # Собираем существующие файлы в массив
    ASSETS=()
    [[ -f "$ANDROID_ASSET" ]] && ASSETS+=("$ANDROID_ASSET")
    [[ -f "$IOS_ASSET" ]] && ASSETS+=("$IOS_ASSET")

    if [ ${#ASSETS[@]} -eq 0 ]; then
        error "No assets found to upload. Check build logs."
        return
    fi

    # Проверяем наличие релиза и создаем/обновляем
    if gh release view "$TAG" >/dev/null 2>&1; then
        substep "Updating existing release ${YELLOW}$TAG${NC}..."
        [[ -n "$prerelease_flag" ]] && gh release edit "$TAG" "$prerelease_flag"
        gh release upload "$TAG" "${ASSETS[@]}" --clobber
    else
        substep "Creating new release ${YELLOW}$TAG${NC}..."
        gh release create "$TAG" \
            --generate-notes \
            --notes-start-tag "${PREVTAG:-$TAG}" \
            $prerelease_flag \
            "${ASSETS[@]}"
    fi
    success "Assets uploaded to GitHub Release"
}

# --- Основной процесс ---
mkdir -p releases
build_android
build_ios
publish_github

echo -e "\n${GREEN}${BOLD}✨ Release $TAG completed successfully!${NC}"