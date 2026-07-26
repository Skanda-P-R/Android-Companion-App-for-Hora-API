# Security & Authorization - Google Identity & Email Whitelisting

This document describes the security architecture implemented in the Hora Companion App as of v0.6.0. The app has moved from a device-bound passwordless identity to a modern verified identity model using Google Sign-In and centralized email whitelisting.

---

## 1. Identity & Authentication

The app leverages **Jetpack Credential Manager** to provide a secure and seamless authentication flow.

*   **Google Sign-In**: Users authenticate using their verified Google accounts. This ensures that the identity is managed by a trusted provider and supports multi-factor authentication (MFA).
*   **ID Token Exchange**: Upon successful sign-in, the app obtains a **Google ID Token** (a signed JWT). This token is sent to the Hora Backend for verification.
*   **Backend Verification**: The server verifies the ID Token using Google's public keys to confirm:
    *   The token was issued by Google (`iss`).
    *   The token is intended for this specific app/server (`aud` matches the Web Client ID).
    *   The token has not expired (`exp`).
*   **User Metadata**: Successful verification allows the server to securely retrieve the user's verified email, name, and profile picture.

## 2. Authorization (Email Whitelisting)

Access to the Hora API is restricted to authorized individuals.

*   **Whitelist Model**: The backend maintains a list of pre-approved email addresses.
*   **Access Denial**: If a user signs in with a Google account whose email is not in the whitelist, the backend returns a `403 Forbidden` response.
*   **Admin Management**: Users are added to the whitelist via administrative CLI commands on the server (`flask add-user <email>`).

## 3. Session Management

*   **Bearer Token Auth**: After verifying the Google identity and checking the whitelist, the server issues a custom session token.
*   **DataStore Persistence**: The session token and basic user profile (name, picture) are stored locally using **Jetpack DataStore**.
*   **Automatic Invalidation**: The app monitors for `401 Unauthorized` responses. If detected, it immediately clears the local session and redirects the user to the login screen.
*   **Logout**: Users can explicitly log out from the Settings screen, which wipes all session and profile data.

## 4. Network Security

*   **HTTPS Enforcement**: All production communication occurs over secure TLS (HTTPS). Cleartext traffic is strictly disabled in the production configuration.
*   **Domain Whitelisting**: The `network_security_config.xml` explicitly whitelists the production domain (`ndaskka.pythonanywhere.com`) to prevent man-in-the-middle attacks.
*   **Logging Control**: Request and response body logging is only enabled in **Debug** builds and is completely disabled in **Release** builds to prevent leaking tokens.

## 5. Data Privacy & Persistence

*   **Anti-Persistence on Uninstall**: The app is configured with `android:allowBackup="false"` and custom data extraction rules. This ensures that when the app is uninstalled, all local data (including credentials and cached charts) is permanently deleted and not restored via cloud backups.
*   **Local Encryption**: Personalized birth charts saved to the device's storage are encrypted using **AES-256**, making them unreadable to other apps or unauthorized file explorers.

---
*Last Updated: 2026-07-26*
