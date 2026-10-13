# AirPointer 1.2 verification

- APK compilation and APK Signature Scheme v3 verification passed.
- Application ID remains com.jimmy.bluepointer; versionCode increased from 2 to 3.
- APK certificate matches the working BluePointer 1.1 APK, allowing an in-place update.
- Existing control regression tests passed: motion integration, stationary noise, axis direction, inversion, reset, gaps, HID limits, report descriptors, LED output, and retry policy.
- Bluetooth session logic, report descriptor and motion engine are preserved from 1.1. Only visible notification and diagnosis names changed in LinkService; the SDP name remains unchanged to preserve host compatibility.
- Physical Galaxy Flip7 / Windows 11 end-to-end retest is pending the user's installation. No physical Bluetooth test is claimed for this UI revision.

- Emulator visual inspection could not be completed in this runtime; no emulator UI pass is claimed.

- Distribution APK rebuilt without bundled fonts; APK contents inspected and original signing identity verified.
