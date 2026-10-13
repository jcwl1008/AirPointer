# AirPointer 1.7 verification

Completed:
- Gradle Android build completed successfully, target/compile API 35, min API 28.
- APK signature verified against the original BluePointer certificate, application ID com.jimmy.bluepointer, versionCode 8 / versionName 1.7.
- APK inspected for bundled fonts and signing keys: none included.
- Arm64 native library inspection found ML Kit 19.0.0 uses 4KB ELF alignment. Built-in ink initialization is disabled on 16KB page devices; a phone-IME handwriting fallback is provided. This APK does not claim full 16KB native-library/Google Play compliance.
- Existing HID/motion/retry regression tests passed.
- Keyboard tests passed: ASCII mappings, Traditional/supplementary Unicode framing, checksum, length, surrogate-safe batching, dictionary/root lookup, related words and stable candidate order.
- Windows receiver compiled against .NET Framework 4.8 reference assemblies.
- Windows frame decoder tested on .NET: valid Traditional Chinese, supplementary Unicode and rejection of wrong checksum/length/malformed surrogate data.

Not yet verified on physical hardware:
- Actual Galaxy Flip7 / Windows 11 HID input, full-screen layout and touch interactions.
- Model download and handwriting recognition on the phone.
- Windows tray app and SendInput behavior in specific target applications.

No physical end-to-end pass or exact Windows 95 candidate order is claimed.

1.4 layout change: removed key-size control, added explicit landscape/portrait toggle, handled orientation configuration in-place, retained draft/IME state/ink and returned presentation to portrait. APK compilation and signature are checked; physical rotation/visual QA is still pending.

1.5: full-area keyboard/trackpad toggle and long-space caret gesture added. Pure Java caret tests cover insert/delete at caret, emoji boundaries, vertical newline navigation and boundary/state restoration. APK build and signing are checked. Physical long-press gesture, soft-wrapped lines, Bluetooth arrow input and visual sizing are not yet verified on the phone.

1.6: media transport packets and Windows VK mappings tested; SpeechRecognizer start/stop lifecycle and stale-callback guards implemented. Microphone permission, actual English/Cantonese recognition and Windows playback response still require physical device validation.

1.7: Calculator-inspired black/gray/orange theme, system 600/400 typography, fixed portrait mode navigation, round media and microphone controls and dark handwriting canvas. ControlTests, DraftEditorTests, KeyboardTests, MediaTests and ThemeTests passed. ThemeTests confirms >=4.5:1 contrast for normal control labels. Gradle compilation and signed APK verification passed; original Bluetooth report descriptor, packet codecs and Windows 1.6 receiver are unchanged. No emulator or physical screenshot/gesture pass is claimed; the approved concept image is not an actual app screenshot.
