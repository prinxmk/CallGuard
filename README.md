# CallGuard v1.2.5

Android 9+ call screening application.

## v1.2.5 stability changes
- Removed the custom `InCallService` from the application manifest. CallGuard now relies on Android's normal incoming-call UI and only performs call screening.
- Hardened `CallScreeningService` so exceptions cannot escape the telecom callback.
- The screening response is sent before blocked-call history is written.
- Database/rule failures safely allow the call rather than crashing the telecom callback.

## Blacklist / whitelist changes
- Browse screens now have a real visible list area.
- Existing entries are shown even when the database contains legacy rule types.
- Every row has visible **Enable/Disable** and **Delete** buttons.
- Search supports partial numbers and prefixes and normalizes common Nigerian formats.
- Database version 3 migrates legacy BLACKLIST/WHITELIST records.
- The same value can exist as a blacklist and whitelist rule because uniqueness is value + rule type.
- Whitelist rules are evaluated before blacklist rules.

## Important Android 9 test
1. Install the new APK over the previous installation.
2. Keep CallGuard selected as the phone's screening/default provider according to the device's settings.
3. Add a test blacklist prefix such as `0803`.
4. Receive a call from a matching test number.
5. Confirm the phone no longer crashes/stops.
6. Open Browse Blacklist and confirm the prefix is visible with Delete.
7. Add the exact test number to Whitelist and confirm it overrides the blacklist prefix.
