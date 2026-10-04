# CallGuard — V1 + V2

CallGuard is an offline Android call-screening application.

## Included

### V1
- Prefix blacklist with unlimited entries
- Exact-number blacklist
- Whitelist
- Block unknown/unsaved callers
- Android CallScreeningService
- Pre-ringing call screening
- Nigerian number normalization
- Local SQLite storage
- Enable/disable protection

### V2
- Blocked-call history
- Search/filter rules
- Edit and delete rules
- Test-number feature
- Contact-aware unknown-caller blocking
- Duplicate detection
- Import/export blacklist and whitelist as CSV
- Quick actions from blocked-call history
- Protection status dashboard

## Build

This repository is designed for GitHub Actions. Push it to GitHub and run:

Actions → Build CallGuard APK → Run workflow

The workflow creates:
- CallGuard-debug
- CallGuard-release-unsigned

## Important Android setup

After installing the APK, open CallGuard and tap **Enable Call Screening**. Android will ask the user to select CallGuard as the call-screening app.

The application uses Android's official `CallScreeningService` API. Android invokes the service for incoming calls before the normal ringing UI and requires the screening response within 5 seconds.

## Rule precedence

1. Whitelist exact number
2. Exact blacklist number
3. Blacklist prefix
4. Saved contact check
5. Block-unknown setting
6. Allow

Whitelist therefore overrides a broader blacklist prefix.

## Notes

The app stores its rules and local blocked-call history on the device. No server is required.

## Android 9 / older-device note

The screening service itself is based on `CallScreeningService`, available from API 24.
On Android 10+ the app can request the `ROLE_CALL_SCREENING` role directly.
On older supported Android versions, the user may need to select CallGuard through the device's Phone/Default-app or caller-ID/spam settings, depending on the manufacturer.

The "Block unknown callers" option requests Contacts access because the app must determine whether an incoming number is saved in the user's contacts. If Contacts permission is denied, unknown-caller blocking cannot reliably distinguish saved from unsaved numbers.

## Android 9 / Infinix compatibility mode

CallGuard now has a dedicated Android 7–9 path. Android 9 does not provide the Android 10 `ROLE_CALL_SCREENING` API, so the Enable button uses the Android 9 default-dialer flow instead. CallGuard declares an ACTION_DIAL activity and an InCallService, and asks Android to make CallGuard the default Phone app. Once selected, the existing CallScreeningService performs the blacklist/prefix/whitelist screening.

Android 10+ continues to use the native Call Screening role.

Important: Android 9 manufacturer software can present the default-phone selection UI differently. The app can request the standard Android default-dialer dialog, but the exact Infinix/XOS wording and location are controlled by the device software.

## V2.1 Android 9 diagnostics

This build adds an Android 9 diagnostic screen. Android's public `ROLE_CALL_SCREENING` API was introduced in API 29, while `CallScreeningService` exists from API 24. On Android 9 the app therefore does not claim that it can select itself as the screening provider through RoleManager. The diagnostic reports the installed screening service, API level, and current default Phone package so testing on manufacturer-specific Android 9 builds can be done without silently replacing the system dialer.


## Android 9 dialer eligibility patch (v1.2.2)

The Android 9 compatibility build now declares the ACTION_DIAL intent with the `tel` URI scheme in addition to the generic ACTION_DIAL declaration. This improves compatibility with Android 9/OEM default-phone-app selection logic that identifies eligible dialer applications from the dial intent filter. Android's documented default phone requirements include ACTION_DIAL handling and a fully implemented InCallService.


## v1.2.3 rule-engine fix
- Blacklist rules are stored as `PREFIX` or `EXACT`, while the UI labels the group `BLACKLIST`.
- v1.2.3 fixes blacklist listing/counting and makes the RuleEngine retrieve both PREFIX and EXACT blacklist rules.
- This fixes Test a Number and live screening decisions that previously saw an empty blacklist.


## v1.2.4 stability and rule-management patch
- Stopped the custom InCallService from launching its own incoming-call activity; the service remains declared for Android 9 default-phone compatibility, while the system/OEM call UI remains in control.
- Hardened CallScreeningService and rule evaluation so database/contacts errors fail open instead of crashing the telecom callback.
- Database migration allows the same normalized value to exist as both a blacklist and whitelist rule, with whitelist precedence.
- Whitelist now supports both exact numbers and prefixes.
- Browse Blacklist and Browse Whitelist now have Add, search, enable/disable, delete, and whitelist actions.
- Search supports the stored normalized number/prefix representation.
