# CallGuard 1.3.1

Android 9+ call and message protection build.

## Highlights
- Android 9-safe CallScreeningService; no API-29-only call-direction APIs in the telecom callback.
- Animated splash screen with version and author credit.
- Modern card-based main UI.
- Browse/search/delete/enable blacklist and whitelist entries.
- Clear blocked-call history.
- Read the Phone (System) call log after permission and add callers directly to blacklist/whitelist.
- High-priority heads-up notifications for blocked calls and blocked messages.
- Optional SMS blocking for blacklisted numbers/prefixes.

## SMS limitation
Reliable SMS interception/removal requires CallGuard to be the device's default SMS application. The app therefore asks the user to explicitly grant the SMS role. When CallGuard is the default SMS app and Message Blocking is enabled, blacklisted messages are not inserted into the SMS inbox; allowed messages are stored in the inbox provider. If another SMS app remains the default, CallGuard does not claim to reliably block SMS.

## Build
Use the included GitHub Actions workflow or run `./gradlew assembleDebug` with Android Gradle Plugin 8.6.1 / compile SDK 35.


### SMS role support in v1.3.1
CallGuard now declares the Android SMS role requirements for SMS_DELIVER, WAP_PUSH_DELIVER (MMS), and SENDTO intents. On Android versions that expose the SMS role, CallGuard can be selected as the default SMS app. The built-in composer can send SMS using SmsManager. Incoming SMS are filtered before being stored in the SMS inbox when message protection is enabled. MMS delivery is accepted by the role-required receiver without attempting to replace Android's provider-level MMS storage.
