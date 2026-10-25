# TITAN CORE X — V3 Security Architecture (Beta)

## Architecture decision
**Keep the existing Android native app while migrating feature-by-feature to Kotlin.** The codebase already has Android Activities, a SAF-driven APK inspector, Android Keystore-backed storage, DNS-only VpnService and device telemetry. A rewrite to Flutter would not grant any additional Android permissions or improve malware detection. Later, use Kotlin + Coroutines + Room + Jetpack Compose for new screens, and optionally Rust through JNI for a *benchmarked* parser that cannot be safely implemented in managed code.

### Clean Architecture boundaries
```text
Presentation / UI (Android Java today, Compose later)
  MainActivity, V3SecurityScanActivity, V3NetworkMonitorActivity,
  PrivacyAuditActivity, ApkInspectorActivity, TitanV3OrbView
       |
Domain / use-cases (no UI mutation from background threads)
  TitanSecurityPosture, ThreatEngine, ApkRiskAnalyzer
       |
Data / platform adapters
  PackageManager (Android visibility restricted)
  ConnectivityManager + TrafficStats (device-level)
  DocumentProvider SAF -> TitanSampleVault (Android Keystore AES-GCM)
  TitanDnsVpnService (user-approved DNS-only VPN; no app traffic firewall yet)
       |
Future optional services
  Verified signed reputation feed, HIBP-style opt-in breach checks,
  a carefully assessed on-device ML classifier
```

## Wireframe — V3 dashboard
```text
┌────────────────────────────────────┐
│ TITAN CORE X     CYBER DEFENSE V3  │
│ ● Internet validated / uncertain   │
│ DNS: user-managed settings          │
│                                    │
│            ◉  SECURITY ORB         │
│          Tap to run audit           │
│      [ Run security checks ]        │
├────────────────┬───────────────────┤
│ Security Audit │ Network Shield    │
│ Local checks   │ DNS / Wi-Fi       │
├────────────────┼───────────────────┤
│ Privacy Matrix │ Performance Lab   │
│ App privileges │ Battery / RAM     │
├────────────────┴───────────────────┤
│ Live Android telemetry (samples)   │
│ Device battery, temperature, RAM   │
└────────────────────────────────────┘
```

## Wireframe — V3 scan
```text
┌────────────────────────────────────┐
│ TITAN SECURITY RADAR               │
│ Only accessible Android checks     │
│           ◉  4 / 6                 │
│       [ Re-run local audit ]        │
│ ▣ Screen lock: pass / review       │
│ ▣ USB debugging: pass / review     │
│ ▣ Developer settings              │
│ ▣ Security patch age               │
│ ▣ Storage headroom                 │
│ ▣ Visible-app permission summary   │
│ [ Inspect user-selected APK ]      │
│ [ Review permission matrix ]       │
└────────────────────────────────────┘
```

## V3 color and typography tokens

| Token | Color | Use |
| --- | --- | --- |
| Obsidian | `#0B0F19` | Screen background |
| Graphite | `#151D2B` | Glass-like surface |
| Surface raised | `#1C293A` | Interactive tiles |
| Emerald | `#10B981` | Passed check, action |
| Electric cyan | `#06B6D4` | Network, active graphs |
| Amber | `#F59E0B` | Needs user review |
| Crimson | `#FB7185` | Confirmed event / destructive confirmation |
| Ivory | `#EDF9FF` | Primary text |
| Muted | `#96AFC1` | Secondary text |

Use **system sans-serif / Roboto**, Arabic system fallback (typically Noto Naskh Arabic or Noto Sans Arabic based on device), sans-serif-black for short heads. Use at least 14sp body copy, 48dp tap targets, RTL and accessibility descriptions. Pulse animation is decorative only, stops when screen is hidden and respects reduced motion/power saver.

**Never display a fabricated percentage such as “100% Protected.”** A numeric 4/6 means four checks completed, not probability of being safe. A signature match can be labeled “confirmed known-hash match” only for a verified, current source; it still requires context.

## Implemented in this development pass
- `TitanSecurityPosture`: real screen lock, ADB, developer mode, security patch date, storage headroom, visible-app permission count.
- `V3SecurityScanActivity`: animated progress reflecting real checks and results.
- `TitanV3OrbView`: lifecycle-conscious Canvas orb without fictitious malware claims.
- `V3NetworkMonitorActivity`: device-wide network byte counters sampled at 1 Hz, visual download/upload rates, transport/validated state.
- `TitanSampleVault`: opt-in private AES-256-GCM encryption of selected sample copies; bounded stream and user-controlled deletion. **Does not sandbox, uninstall, disable, or delete original APK.**
- Reworked dark dashboard and Bento shortcuts.
- Existing `ThreatEngine`: SHA-256 equality against an authenticated local feed; `ApkRiskAnalyzer`: explainable static heuristics.
- Remote feed updater disabled because a valid signed feed and trust provisioning are not currently available.

## Not implemented / why
- Full antivirus: needs licensed or vetted signature corpus, tested update signing, malware corpus evaluation, false-positive measurement, and APK extraction constraints.
- “On-device AI” classification: no validated model, dataset, or runtime shipped. Static permission heuristics are **not ML**.
- Automatic interception of every downloaded file and private app data: Android sandbox / Play policies prevent this for ordinary apps.
- Per-app kill switch: would need a fully working, user-consented `VpnService` that forwards or rejects IP flows correctly. Current service is **DNS-only**.
- DNS-over-HTTPS for every app: apps can use their own encrypted DNS, and Android Private DNS may bypass the local resolver approach.
- Guaranteed MITM / ARP spoofing detection: the app can inspect gateway, DNS, certificate anomalies, and suspicious changes, but cannot conclusively prove attacks from a single ordinary app check.
- Camera/microphone usage attribution by all apps: requires special OS-level access, not available to ordinary apps.
- Silent intruder photos or covert location collection: intentionally excluded on privacy grounds.
- Breach-radar credential checks: needs approved provider integration, verified user ownership, rate limits, and consent.

## OWASP MASVS acceptance targets
- **MASVS-STORAGE:** encrypted locally cached sensitive samples; no external plain-text cache; delete control.
- **MASVS-CRYPTO:** AndroidKeyStore AES-256-GCM with fresh IV per import; protect against tampering, corrupted samples and key invalidation.
- **MASVS-NETWORK:** HTTPS + platform certificate checks; do not claim full VPN when routing DNS only.
- **MASVS-PLATFORM:** SAF permissions and Android package-visibility restrictions; avoid requesting unrelated powerful permissions.
- **MASVS-CODE:** background thread file parsing, 250 MiB cap and ZIP anomaly checks; verify integrity before interpreting feeds.
- **MASVS-RESILIENCE:** release certificate fingerprint gate and tamper verification where configured.
- **MASVS-PRIVACY:** opt-in monitoring; no secret photos/location data collection, no undisclosed tracking.

References:
- https://mas.owasp.org/MASVS/
- https://developer.android.com/reference/android/net/VpnService
- https://developer.android.com/training/package-visibility/declaring

## QA gates before general release
1. Gradle `:app:assembleDebug` and `:app:assembleRelease` pass, including minify.
2. Start, background, return, rotate and close each activity; confirm no dangling background UI work.
3. Test screen lock and ADB detection on physical Android versions 8.0–15+.
4. Test malformed/oversized/non-APK SAF files; ensure temporary files removed and no OOM.
5. Encrypt sample, verify non-plaintext on disk, delete and verify removal; test keystore invalidation.
6. Test data counter unsupported (-1), counter reset, airplane mode, VPN and switch Wi-Fi/cellular.
7. DNS-only VPN fail-open/fail-closed behavior must be documented and tested per packet type.
8. Baseline accessibility: TalkBack labels, 48dp targets, font scale 200%, reduced motion, RTL.
9. Threat accuracy: signed feed provenance, expiration/revocation policies, false-positive and false-negative benchmarking.
10. No “virus-free,” “100% protection,” or “per-app firewall” claims until separately verified.
