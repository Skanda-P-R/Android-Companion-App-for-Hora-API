# Security & Authorization - Device Identity

This document describes the security architecture implemented in the HoraJnana App. The app uses a device-based identity model for simplified and secure access.

---

## 1. Identity & Authentication

The app uses the unique **Android ID** (hashed as a UUID) to provide a secure authentication flow.

*   **Device-Based Auth**: Users are identified by their device's unique identifier. This removes the need for personal accounts (like Google or Email) while maintaining a consistent session for the device.
*   **UUID Verification**: The app sends the device UUID to the Hora Backend for authentication.
*   **Backend Verification**: The server verifies the device identifier to confirm access and issue a session token.

## 2. Session Management

*   **Bearer Token Auth**: After verifying the device identity, the server issues a custom session token.
*   **DataStore Persistence**: The session token is stored locally using **Jetpack DataStore**.
*   **Automatic Invalidation**: The app monitors for `401 Unauthorized` responses. If detected, it immediately clears the local session and redirects the user to the login screen.
*   **Logout**: Users can explicitly log out from the Settings screen, which wipes the session data.

## 4. Network Security

*   **HTTPS Enforcement**: All production communication occurs over secure TLS (HTTPS). Cleartext traffic is strictly disabled in the production configuration.
*   **Domain Whitelisting**: The `network_security_config.xml` explicitly whitelists the production domain (`ndaskka.pythonanywhere.com`) to prevent man-in-the-middle attacks.
*   **Logging Control**: Request and response body logging is only enabled in **Debug** builds and is completely disabled in **Release** builds to prevent leaking tokens.

## 5. Data Privacy & Persistence

*   **Anti-Persistence on Uninstall**: The app is configured with `android:allowBackup="false"` and custom data extraction rules. This ensures that when the app is uninstalled, all local data (including credentials and cached charts) is permanently deleted and not restored via cloud backups.
*   **Local Encryption**: Personalized birth charts saved to the device's storage are encrypted using **AES-256**, making them unreadable to other apps or unauthorized file explorers.

---
*Last Updated: July 31, 2026*
