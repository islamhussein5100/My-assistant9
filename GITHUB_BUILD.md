# MayaAssistant — GitHub APK Build (Phone Only)

This project is prepared for GitHub Actions. You do not need Android Studio or an Android SDK on your phone.

## Phone steps
1. Create a GitHub account and a new **Public** repository (for easiest free Actions use).
2. Upload **all files inside this folder** to the repository root. Do not upload this outer folder itself as the only item.
3. Open the repository's **Actions** tab.
4. Select **Build MayaAssistant APK**.
5. Tap **Run workflow** (or push to `main`/`master`).
6. Wait for the green check.
7. Open the completed workflow run and scroll to **Artifacts**.
8. Download `MayaAssistant-debug-apk.zip`, extract it, and install the APK on Android.

The workflow uses GitHub-hosted Ubuntu, JDK 17, Gradle 8.7, and builds `:app:assembleDebug`.

## Important
- This produces a **debug APK** for testing, not a Play Store release package.
- Android permissions still have to be granted by the user after installation.
- The screen-off Hey Maya listener is best-effort and may be restricted by some phone manufacturers' battery management.
