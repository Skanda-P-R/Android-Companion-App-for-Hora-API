# Security Implementation — Passwordless Device-Bound Authentication

This document describes the security architecture implemented in the Hora Companion App as of v0.3.0.

---

## 1. Device Identity & Persistence

To ensure a seamless user experience while maintaining security, the app implements a hardware-tied device identity.

*   **Stable Identifier**: The app utilizes `Settings.Secure.ANDROID_ID` to identify the device. This identifier is unique to the device and the app's signing key.
*   **Persistence across Reinstalls**: By using the hardware-tied ID, the device identity remains consistent even if the app is uninstalled and reinstalled.
*   **UUID Conversion**: For server compatibility, the `ANDROID_ID` is hashed into a standard UUID format using `UUID.nameUUIDFromBytes()`. This ensures a fixed 128-bit identifier is always sent for the same device.

## 2. Session Management

The app manages user sessions using a combination of Jetpack DataStore and a specialized Repository pattern.

*   **Token Storage**: The session token (JWT/Bearer) is stored in Jetpack DataStore (`auth_settings`), providing an asynchronous and consistent storage mechanism.
*   **One-Time Login**: Once a user successfully logs in, the session token is persisted. The user is not required to log in again unless they explicitly log out or the session is invalidated by the server.
*   **Session Expiration Handling**: A `sessionExpiredEvent` (using Kotlin Flows) allows the app to reactively redirect the user to the Login screen if a session becomes invalid during background or foreground operations.

## 3. Network Security

Multiple layers of security are applied to the network communication between the app and the Hora Server.

*   **Bearer Token Injection**: An `AuthInterceptor` automatically attaches the `Authorization: Bearer <token>` header to all outgoing API requests (excluding the login endpoint).
*   **Automatic Invalidation**: A `SessionInvalidationInterceptor` monitors for `401 Unauthorized` responses. Upon detection, it clears the local session and triggers the login flow.
*   **Network Security Configuration**:
    *   **Cleartext Traffic**: Completely disabled in the manifest. All communication must occur over HTTPS.
    *   **Domain Whitelisting**: Explicit domain configuration for `ndaskka.pythonanywhere.com` to prevent man-in-the-middle risks.
*   **Smart Logging Control**: Network logs (request/response bodies) are strictly limited to **Debug builds** only. In **Release builds**, all logging is disabled to prevent leakage of sensitive tokens or device identifiers.

## 4. Authentication Flow

*   **Passwordless Experience**: Users are authenticated based on their username and the device's hardware identity.
*   **Device Binding**: The server validates that the username is associated with the specific `device_uuid` provided during login.
*   **User Feedback**: The app parses structured API error responses (e.g., `device_mismatch`) to provide clear, actionable feedback to the user during the login process.

---

## 5. Security Best Practices Observed

*   **Hardware-Tied Keys**: Usage of `ANDROID_ID` linked to the signing certificate.
*   **Credential Masking**: Tokens are never logged in production.
*   **Encrypted Storage Potential**: While `ANDROID_ID` is used for identity, any sensitive user-specific secrets beyond the session token are handled via Android Keystore-backed mechanisms where applicable.
