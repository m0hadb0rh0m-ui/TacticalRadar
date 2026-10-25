# TITAN CORE X — Dual-channel release security

## Signing architecture
1. Enroll in Google Play App Signing using a Google-managed app signing key.
2. Generate and protect a separate upload key outside the repository. Never upload private keystores or passwords to GitHub.
3. Build a release AAB signed with the upload key and upload it to Play Console.
4. Download the **signed universal APK** for that release from Play Console and publish that exact artifact on GitHub Releases. Do not sign the GitHub APK with the upload key.
5. Record and independently verify SHA-256 of the APK, and the signing certificate fingerprint using apksigner verify --print-certs.
6. Configure TITAN_SIGNING_SHA256 to the **app signing** certificate fingerprint for release runtime checks; do not use the upload certificate fingerprint.
7. Keep GitHub Actions debug APK artifacts clearly marked as testing-only. Never publish debug APK as production.
8. Configure Play Integrity server-side verification before gating sensitive operations. Client-side verdicts alone are insufficient.

## Release security gate
- Run assembleDebug and assembleRelease (with protected signing credentials available for the latter).
- Review exported components, permission boundaries, WebView interfaces, network security, and backup rules.
- Scan tracked source and full repository history for exposed secrets, and rotate any discovered credentials.
- Verify Play and GitHub APKs use the same package name and signing certificate.
- Test clean install and update in both directions on supported Android versions.
- Perform authorized mobile security assessment following OWASP MASVS/MASTG.
- Do not claim penetration testing passed without reproducible test evidence.

## Operational cautions
- A SHA-256 fingerprint is public; a private signing key is secret.
- The local signing fingerprint check is a defense-in-depth signal, not tamper-proof.
- Keystore helper exists, but all sensitive call sites must be migrated and tested.
- Never check in .jks, .keystore, passwords, or CI credentials.
