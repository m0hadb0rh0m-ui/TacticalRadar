# TITAN CORE X — Android v0.1.0

A **native Android** device intelligence dashboard (Java, Android Gradle Plugin 8.7.3).

## Working in the first APK
- Premium dark cyan navigation and live health score based on RAM availability, battery temperature and free storage.
- Device diagnostics: manufacturer, model, Android version, CPU core count, RAM and storage.
- Battery analyzer: level, charging state, battery temperature, voltage, and charging current when exposed by hardware.
- Security review: count of visible launchable apps, direct access to Android permissions and security settings.
- Gaming preparation: memory/temperature/battery readouts and links to background-app and Do Not Disturb settings.
- Network inspector: connection transport, advertised Internet capability, Android validation status.
- Storage inspector: capacity and free space, Android storage manager and file picker.
- No sensitive data sent to a server; no Internet permission requested.

## Honest limits
This is not an antivirus engine, not a root cleaner, not a mobile signal amplifier, and does not force-kill third-party apps. The app cannot promise FPS boosts or access restricted storage. The security screen clearly marks malware detection as unavailable.

## Build
Use GitHub Actions workflow **Build TITAN CORE X APK** on branch `titan-core-x`. Download the artifact **TITAN-CORE-X-APK** from a successful run, unzip, and install `app-debug.apk`.

## Roadmap
Phase 2: Arabic localization, custom charts, permission audit details, battery history, game session journal, duplicate-file scanner using Storage Access Framework.
Phase 3: opt-in network tests, optional threat intelligence integration with clear privacy disclosures, robust reports.
