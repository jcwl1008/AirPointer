#!/usr/bin/env bash
set -euo pipefail
# Requires Android SDK platform 35/build tools 35.0.0 and Java 17 or later.
cd "$(dirname "$0")"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$PWD/tools/android-sdk}}"
BT="$SDK_ROOT/build-tools/35.0.0"
JAR="$SDK_ROOT/platforms/android-35/android.jar"
mkdir -p build/classes build/dex deliverables tools
if command -v javac >/dev/null; then
 javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$JAR:$BT/core-lambda-stubs.jar" -d build/classes app/src/com/jimmy/bluepointer/*.java
else
 if [ ! -f tools/ecj.jar ]; then curl --fail -L -o tools/ecj.jar https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.37.0/ecj-3.37.0.jar; fi
 java -jar tools/ecj.jar -encoding UTF-8 -nowarn -1.8 -bootclasspath "$JAR:$BT/core-lambda-stubs.jar" -d build/classes app/src/com/jimmy/bluepointer/*.java
fi
"$BT/d8" --lib "$JAR" --min-api 28 --output build/dex build/classes/com/jimmy/bluepointer/*.class
"$BT/aapt" package -f -M app/AndroidManifest.xml -S app/res -A app/assets -I "$JAR" -F build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
# Supply your own keystore. Never commit a signing key.
KEYSTORE="${AIRPOINTER_KEYSTORE:?Set AIRPOINTER_KEYSTORE to your keystore path}"
KEY_ALIAS="${AIRPOINTER_KEY_ALIAS:-airpointer}"
: "${AIRPOINTER_KEYSTORE_PASSWORD:?Set AIRPOINTER_KEYSTORE_PASSWORD}"
export AIRPOINTER_KEYSTORE_PASSWORD
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass env:AIRPOINTER_KEYSTORE_PASSWORD --ks-key-alias "$KEY_ALIAS" --out deliverables/AirPointer-v1.2.apk build/aligned.apk
"$BT/apksigner" verify --verbose deliverables/AirPointer-v1.2.apk
