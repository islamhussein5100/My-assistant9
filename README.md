# Maya Assistant — Phone Build Edition

This ZIP is prepared specifically for building on an Android phone with **Code on the Go (CoGo)**.

## Why this version is easier
- Uses AGP 8.11.0 and Kotlin 1.9.22, matching CoGo's current on-device toolchain.
- Removes AndroidX/Material dependencies; the app uses Android framework UI only.
- Kotlin/Java target is 17.
- No PC is required.

## Build on phone
1. Install **Code on the Go** from the official App Dev for All site.
2. Open CoGo → Import/Open an existing project.
3. Select this extracted `MayaAssistant-PhoneBuild` folder.
4. Let the project sync/index.
5. Build the **debug APK** (`assembleDebug`) from Build/Run.
6. Install the generated APK on the phone.

## Important
The screen-off listener is best-effort. Android/OEM battery management can stop long-running microphone services. It is not an OEM DSP hotword system.

## Main features
- Bengali voice input (bn-BD)
- Bengali TTS
- Hey Maya best-effort screen-off listener
- Default Android Assistant integration
- YouTube / Facebook / Google commands
- Calls, SMS composer, contacts, camera, location, Wi-Fi/Bluetooth settings, volume commands

If CoGo shows a build error, send a screenshot of the error and the ZIP can be adjusted.
