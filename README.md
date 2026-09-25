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
