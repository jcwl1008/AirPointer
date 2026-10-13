# Sources and third-party components

Unicode Unihan 18.0.0, retrieved 2026-10-13:
- https://www.unicode.org/Public/UCD/latest/ucd/Unihan.zip
- kCangjie, kFrequency, kTraditionalVariant; converted by scripts/build_quick_dictionary.py.
- Generated assets quick.tsv and traditional.tsv distributed under UNICODE-LICENSE.txt.
- This is not a Windows 95 Microsoft Quick table. Candidate order is fixed Unicode frequency then codepoint, not an exact Microsoft legacy order.

Google ML Kit Digital Ink Recognition 19.0.0:
- https://developers.google.com/ml-kit/vision/digital-ink-recognition/android
- https://developers.google.com/android/reference/com/google/mlkit/vision/digitalink/recognition/DigitalInkRecognitionModelIdentifier
- Chinese zh-Hant request may match zh-Hani; candidate output is normalized through the distributed Unicode variant mapping.
- Terms: https://developers.google.com/ml-kit/terms
- SDK data disclosure: https://developers.google.com/ml-kit/android-data-disclosure

Existing Bluetooth report source notices retained in THIRD-PARTY-NOTICES.txt.
No Samsung or Apple font binaries are included.
