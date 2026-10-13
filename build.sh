#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
# Requires JDK 17, Android SDK 35/build-tools 35.0.0 and Gradle 8.9.
gradle --no-daemon :app:assembleDebug
mkdir -p deliverables
SDK_ROOT="${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT}"
BT="$SDK_ROOT/build-tools/35.0.0"
: "${AIRPOINTER_KEYSTORE:?Set AIRPOINTER_KEYSTORE to your private signing key}"
: "${AIRPOINTER_KEYSTORE_PASSWORD:?Set AIRPOINTER_KEYSTORE_PASSWORD}"
export AIRPOINTER_KEYSTORE_PASSWORD
"$BT/apksigner" sign --ks "$AIRPOINTER_KEYSTORE" --ks-pass env:AIRPOINTER_KEYSTORE_PASSWORD --ks-key-alias "${AIRPOINTER_KEY_ALIAS:-airpointer}" --out deliverables/AirPointer-v1.7.apk app/build/outputs/apk/debug/app-debug.apk
"$BT/apksigner" verify --verbose deliverables/AirPointer-v1.7.apk
