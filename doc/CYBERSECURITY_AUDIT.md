# HabitFlow — Cybersecurity, Privacy & Data Leakage Audit

## Executive Summary

A comprehensive static security audit of the **HabitFlow** Android application repository (`com.aistudio.habitflow.kxmpzq`) was conducted across source code, manifest configurations, database architectures, background components, build configurations, and CI/CD pipelines.

The audit verified that while HabitFlow is currently an **offline-first local application** with no active remote web servers or network communications, several critical security and privacy flaws exist in how the application manages data persistence, backup extraction, sensitive recovery passkeys, device-level data protection, and release build hardening.

### Risk Distribution Summary
- **Critical Severity**: 0
- **High Severity**: 2
- **Medium Severity**: 4
- **Low Severity**: 4
- **Total Audited Findings**: 10

---

## Detailed Vulnerability & Risk Findings

---

### Finding SEC-01: Cleartext Recovery Passkey & Sensitive PII Stored Unencrypted in SQLite and SharedPreferences
- **Severity**: **High**
- **OWASP Category**: M2: Insecure Data Storage / M5: Insufficient Cryptography
- **CWE**: CWE-312: Cleartext Storage of Sensitive Information
- **Affected File & Line Numbers**:
  - `app/src/main/java/com/example/data/repository/BackupRecoveryManager.kt#L41-L56, L105, L281-L295`
  - `app/src/main/java/com/example/data/model/UserProfileEntity.kt#L10-L12`
  - `app/src/main/java/com/example/data/db/AppDatabase.kt#L36-L54`

#### Evidence & Risk Explanation
In `BackupRecoveryManager.kt`, the application extracts the user profile from Room and builds a JSON snapshot containing:
```kotlin
val profileObj = JSONObject().apply {
    put("userName", profile.userName)
    put("userEmail", profile.userEmail)
    put("syncKey", profile.syncKey) // Cleartext recovery passkey!
    // ...
}
```
This snapshot is then directly written without encryption to:
1. Android private `SharedPreferences` under the file `habitflow_cloud_backup_prefs.xml`:
   ```kotlin
   val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
   val editor = prefs.edit().putString(KEY_LATEST_SNAPSHOT, json)
   if (!accountKey.isNullOrBlank()) {
       editor.putString(KEY_ACCOUNT_SNAPSHOT_PREFIX + accountKey.lowercase(), json)
   }
   ```
2. A plain internal JSON file: `File(ctx.filesDir, "habitflow_recovery_snapshot.json").writeText(json)`.
3. The local SQLite database (`habitflow.db`), table `user_profile`, column `syncKey`.

If a user enters a secret password or passkey into the "Recovery Passkey" field in the UI, it is stored in cleartext across three separate local storage locations. Users frequently reuse master passwords as recovery passkeys, creating a severe credential exposure hazard.

#### Realistic Attack Scenario
A user loses their unlocked phone or leaves it unattended. A bad actor, or a malicious app exploiting a local directory traversal or local backup access, reads `/data/data/com.aistudio.habitflow.kxmpzq/shared_prefs/habitflow_cloud_backup_prefs.xml`. The attacker extracts the user's primary email address and secret passkey, which they then attempt against the victim's email, banking, or cloud accounts.

#### Recommended Fix & Implementation
Never store passkeys or secret credentials in plaintext. If the passkey is intended for local authentication or recovery verification, store only an irreversible cryptographic hash (using Argon2id or PBKDF2 with a random salt). For local key-value storage, use Android Jetpack Security (`EncryptedSharedPreferences`).

```kotlin
// Implementation Example: EncryptedSharedPreferences with Android Keystore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun getSecurePreferences(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "habitflow_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
```

#### Verification Steps
1. Build and launch the patched app, set a recovery passkey, and save.
2. Inspect the filesystem via ADB: `adb shell "run-as com.aistudio.habitflow.kxmpzq cat /data/data/com.aistudio.habitflow.kxmpzq/shared_prefs/habitflow_cloud_backup_prefs.xml"`.
3. Verify that `syncKey` is absent from plaintext files and that stored values are fully encrypted ciphertext.

---

### Finding SEC-02: Full ADB & Cloud Backup Extraction Enabled Without Exclusion Rules
- **Severity**: **High**
- **OWASP Category**: M2: Insecure Data Storage
- **CWE**: CWE-538: Insertion of Sensitive Information into Externally-Accessible File or Directory
- **Affected File & Line Numbers**:
  - `app/src/main/AndroidManifest.xml#L11-L13`
  - `app/src/main/res/xml/backup_rules.xml#L1-L7`
  - `app/src/main/res/xml/data_extraction_rules.xml#L1-L14`

#### Evidence & Risk Explanation
In `AndroidManifest.xml`, backup is explicitly enabled:
```xml
android:allowBackup="true"
android:dataExtractionRules="@xml/data_extraction_rules"
android:fullBackupContent="@xml/backup_rules"
```
Furthermore, `backup_rules.xml` and `data_extraction_rules.xml` explicitly include all databases, shared preferences, and private files:
```xml
<full-backup-content>
    <include domain="database" path="." />
    <include domain="sharedpref" path="." />
    <include domain="file" path="." />
</full-backup-content>
```
On Android, enabling `allowBackup="true"` allows anyone with physical access and USB debugging enabled to execute `adb backup -f backup.ab -noapk com.aistudio.habitflow.kxmpzq` and dump the complete SQLite database, SharedPreferences, and internal files without requiring root privileges. Moreover, cloud backups to Google Drive will upload unencrypted user habit histories and stored passkeys to third-party cloud infrastructure.

#### Realistic Attack Scenario
An employee connects their mobile device to an untrusted computer or airport charging kiosk that has active ADB pairing. The station initiates an automated ADB backup, pulling the user's private daily routine records, health tracking data, and unencrypted passkeys from the device storage.

#### Recommended Fix & Implementation
If cloud or ADB backups are not an explicit business requirement, set `android:allowBackup="false"` in `AndroidManifest.xml`. If backup is required, strictly exclude sensitive directories, credentials, and databases:

```xml
<!-- In res/xml/data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="habitflow.db" />
        <exclude domain="sharedpref" path="habitflow_cloud_backup_prefs.xml" />
        <exclude domain="file" path="habitflow_recovery_snapshot.json" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="habitflow.db" />
        <exclude domain="sharedpref" path="habitflow_cloud_backup_prefs.xml" />
        <exclude domain="file" path="habitflow_recovery_snapshot.json" />
    </device-transfer>
</data-extraction-rules>
```

#### Verification Steps
1. Connect device with USB debugging enabled.
2. Run `adb backup -f backup.ab com.aistudio.habitflow.kxmpzq`.
3. Unpack `backup.ab` using `abe.jar` (Android Backup Extractor) and confirm that `habitflow.db` and sensitive SharedPreferences files are completely absent.

---

### Finding SEC-03: Lack of At-Rest SQLite Database Encryption
- **Severity**: **Medium**
- **OWASP Category**: M2: Insecure Data Storage
- **CWE**: CWE-311: Missing Encryption of Sensitive Data
- **Affected File & Line Numbers**:
  - `app/src/main/java/com/example/data/db/AppDatabase.kt#L60-L73`

#### Evidence & Risk Explanation
`AppDatabase.kt` builds the standard SQLite Room database:
```kotlin
Room.databaseBuilder(
    context.applicationContext,
    AppDatabase::class.java,
    "habitflow.db"
)
.addMigrations(MIGRATION_2_3)
.fallbackToDestructiveMigration()
.build()
```
The database stores personal, medical, or lifestyle habits (e.g., medication routines, therapy, health goals, emotional habits) and daily completion timestamps in standard plaintext SQLite. On rooted devices, secondary storage backups, or compromised devices, any tool (such as `sqlite3`) can open and inspect `habitflow.db` directly.

#### Realistic Attack Scenario
A user installs a malicious application that gains root access or exploits a known local kernel vulnerability. The malware copies `/data/data/com.aistudio.habitflow.kxmpzq/databases/habitflow.db` to its own storage, parsing the user's behavioral habits and daily lifestyle records for profile building or blackmail.

#### Recommended Fix & Implementation
Integrate **SQLCipher for Android** (`net.zetetic:sqlcipher-android`) with Room to encrypt the entire SQLite database file with 256-bit AES-GCM, storing the encryption key inside the Android Keystore.

```kotlin
// Example SQLCipher integration with Room SupportOpenHelperFactory
import net.sqlcipher.database.SupportFactory

val passphrase = getOrCreateKeystoreMasterSecret() // Retrieved from Android Keystore
val factory = SupportFactory(passphrase)

val db = Room.databaseBuilder(context, AppDatabase::class.java, "habitflow.db")
    .openHelperFactory(factory)
    .addMigrations(MIGRATION_2_3)
    .build()
```

#### Verification Steps
1. Pull `habitflow.db` from device storage.
2. Attempt to open it via `sqlite3 habitflow.db "SELECT * FROM habits;"`.
3. Confirm that SQLite returns `file is not a database` or `database disk image is malformed` due to ciphertext encryption.

---

### Finding SEC-04: Pseudonymous "Cloud Sync" with Insecure Account Switching on Shared Devices
- **Severity**: **Medium**
- **OWASP Category**: M1: Improper Platform Usage / M8: Security Misconfiguration
- **CWE**: CWE-287: Improper Authentication
- **Affected File & Line Numbers**:
  - `app/src/main/java/com/example/data/repository/BackupRecoveryManager.kt#L223-L264`
  - `app/src/main/java/com/example/ui/screens/ProfileScreen.kt#L428-L530`

#### Evidence & Risk Explanation
The application UI presents an "Account Sync & Data Recovery" feature with "Account Email" and "Recovery Passkey (optional)" fields, promising to "Keep habits, streaks & history safe across reinstalls and devices".
However, the implementation in `BackupRecoveryManager.kt` contains no network requests, no remote server authentication, and no cryptographic verification:
```kotlin
suspend fun syncAccountData(email: String, syncKey: String): Result<String> = withContext(Dispatchers.IO) {
    val accountId = cleanEmail.ifEmpty { cleanKey }
    val storedAccountJson = loadMirrorSnapshot(accountId)
    if (storedAccountJson != null && currentLogs.isEmpty()) {
        restoreFromSnapshotJson(storedAccountJson).getOrThrow()
        // ...
    }
}
```
1. **Misleading Security Assurance**: Users are led to believe their data is synchronized with a secure remote cloud infrastructure. If the device is lost, stolen, or factory-reset, all user data is permanently lost.
2. **Account Hijacking / IDOR on Multi-User Devices**: On a shared family tablet or borrowed phone, any person who types another user's email into the box and clicks "Sync" can immediately switch the active session to that user's cached snapshot without entering a password or proving account ownership.

#### Realistic Attack Scenario
A user shares a family tablet with siblings or colleagues. Another user types the primary user's known email address into the "Account Email" field and presses "Sync & Recover Account Data". The app automatically restores the primary user's complete private routine list, habits, and passkey into the active view without any authorization check.

#### Recommended Fix & Implementation
1. If the app is intended to remain strictly offline, rename the UI section to **"Local Device Profiles / Offline Snapshots"** and remove claims of cross-device cloud synchronization.
2. If real cloud synchronization is intended, implement true backend authentication (e.g. Firebase Auth with OAuth2 / Google Sign-In via Android Credential Manager) and transfer snapshots via HTTPS TLS 1.3 with JWT authorization headers.

#### Verification Steps
1. On an app instance with existing habit data, enter `test@example.com` and sync.
2. Clear app data or reset database.
3. Enter `test@example.com` without a passkey and click "Sync". Verify whether unauthorized data restoration is permitted or blocked by the new authentication gate.

---

### Finding SEC-05: Sensitive Plaintext Data Exposure via UI Clipboard / Raw JSON Backup Dialog
- **Severity**: **Medium**
- **OWASP Category**: M2: Insecure Data Storage / M9: Insecure Communication
- **CWE**: CWE-200: Exposure of Sensitive Information to an Unauthorized Actor
- **Affected File & Line Numbers**:
  - `app/src/main/java/com/example/ui/screens/ProfileScreen.kt#L693-L724`
  - `app/src/main/java/com/example/data/repository/BackupRecoveryManager.kt#L41-L56`

#### Evidence & Risk Explanation
In `ProfileScreen.kt`, tapping "Export Backup" renders an `AlertDialog` containing an editable/copyable `OutlinedTextField` displaying the raw, unencrypted JSON payload:
```kotlin
OutlinedTextField(
    value = exportedJsonText ?: "",
    onValueChange = {},
    readOnly = true,
    maxLines = 6,
    modifier = Modifier.fillMaxWidth()
)
```
The raw string displayed and copied directly contains:
- `userName`
- `userEmail`
- `syncKey` (cleartext recovery passkey)
- Complete timestamps and titles of every habit tracked.

When the user copies this text, it is placed on the Android system clipboard. In Android versions prior to Android 13 (and even in newer versions with accessibility permissions or keyboard apps), third-party background applications can monitor clipboard contents and extract sensitive credentials and personal habit logs.

#### Realistic Attack Scenario
A user copies their backup JSON to save it into their notes. A third-party keyboard app or a clipboard utility running in the background captures the clipboard buffer, logging the user's email, habits, and passkey to an external telemetry server.

#### Recommended Fix & Implementation
1. Strip all sensitive authentication tokens (`syncKey`) from exported JSON payloads.
2. Use Android's **Storage Access Framework (SAF)** (`Intent.ACTION_CREATE_DOCUMENT`) to export directly to a password-protected, AES-256-GCM encrypted file (`.habitflow`) rather than dumping raw JSON into the UI and clipboard.

```kotlin
// Example: Strip sensitive credentials before export
val safeProfileObj = JSONObject().apply {
    put("userName", profile.userName)
    put("userEmail", profile.userEmail)
    // syncKey is explicitly omitted from exports
    put("livesCount", profile.livesCount)
    // ...
}
```

#### Verification Steps
1. Trigger "Export Backup" in the application.
2. Inspect the exported payload and ensure `syncKey` is omitted.
3. Verify that data is saved via private file streams rather than system clipboard buffers.

---

### Finding SEC-06: Code Obfuscation & Shrinking (R8 / ProGuard) Disabled in Production Release
- **Severity**: **Medium**
- **OWASP Category**: M9: Reverse Engineering
- **CWE**: CWE-656: Reliance on Security Through Obscurity (Failure to Protect Code Assets)
- **Affected File & Line Numbers**:
  - `app/build.gradle.kts#L44-L51`
  - `app/proguard-rules.pro#L1-L22`

#### Evidence & Risk Explanation
In `app/build.gradle.kts`, the release build configuration explicitly disables R8 code shrinking and obfuscation:
```kotlin
buildTypes {
    release {
        isCrunchPngs = false
        isMinifyEnabled = false // Obfuscation disabled!
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        signingConfig = signingConfigs.getByName("release")
    }
}
```
Furthermore, `app/proguard-rules.pro` contains only commented-out template instructions.
When `isMinifyEnabled = false`:
1. Compiled DEX bytecode retains all original class names, method signatures, package structures, and variable names.
2. Decompilers (such as JADX, Bytecode Viewer, or Ghidra) reconstruct near-source-level Kotlin code with zero obfuscation.
3. Attackers can analyze internal database table names, SharedPreferences keys (`habitflow_cloud_backup_prefs`), and logic constraints in minutes.

#### Realistic Attack Scenario
An attacker downloads the published release APK from GitHub Releases or an app store, runs `jadx -d out app-release.apk`, and immediately discovers the exact SharedPreferences file names, JSON keys, and gamification bypass rules, facilitating targeted local data tampering.

#### Recommended Fix & Implementation
Enable R8 shrinking and obfuscation in `app/build.gradle.kts` for release builds, and configure `proguard-rules.pro` to keep Room entities and Moshi adapters:

```kotlin
// In app/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

```proguard
# In app/proguard-rules.pro
# Preserve Room Database Entities & DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Preserve Moshi / JSON models if reflection is used
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
```

#### Verification Steps
1. Build the release APK: `./gradlew assembleRelease`.
2. Inspect the generated APK with `dexdump` or `jadx`.
3. Confirm that internal classes (`BackupRecoveryManager`, `HabitRepository`) have their names minified to short identifiers (e.g. `a.b.c`).

---

### Finding SEC-07: Excessive Dangerous Permission Declaration (`SCHEDULE_EXACT_ALARM`)
- **Severity**: **Low**
- **OWASP Category**: M1: Improper Platform Usage
- **CWE**: CWE-250: Execution with Unnecessary Privileges
- **Affected File & Line Numbers**:
  - `app/src/main/AndroidManifest.xml#L8`
  - `app/src/main/java/com/example/receiver/NotificationHelper.kt#L93-L98, L149-L157`

#### Evidence & Risk Explanation
In `AndroidManifest.xml`, the high-privilege permission `SCHEDULE_EXACT_ALARM` is declared:
```xml
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```
However, inspecting `NotificationHelper.kt` reveals that exact alarms are **never used**:
```kotlin
alarmManager.setInexactRepeating(
    AlarmManager.RTC_WAKEUP,
    calendar.timeInMillis,
    AlarmManager.INTERVAL_DAY,
    pendingIntent
)
```
On Android 13+ (API level 33+) and Android 14+ (API level 34+), `SCHEDULE_EXACT_ALARM` is restricted. Apps declaring this permission without meeting strict Google Play core criteria (e.g., calendar or alarm-clock apps) face policy rejections and revocations by Google Play Protect. Because HabitFlow uses `setInexactRepeating`, requesting `SCHEDULE_EXACT_ALARM` introduces unnecessary risk and policy friction.

#### Realistic Attack Scenario
A security scanner or app store automated compliance engine flags HabitFlow for requesting unnecessary privileged system alarm APIs, delaying or rejecting application publishing.

#### Recommended Fix & Implementation
Remove `SCHEDULE_EXACT_ALARM` from `AndroidManifest.xml`. If exact notifications become necessary in the future, use `USE_EXACT_ALARM` (if eligible) or migrate to Android Jetpack **WorkManager** for battery-friendly periodic background tasks.

```xml
<!-- In AndroidManifest.xml: Remove line 8 entirely -->
<!-- <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" /> -->
```

#### Verification Steps
1. Remove the permission from `AndroidManifest.xml`.
2. Build and launch the APK on an Android 14+ device.
3. Trigger habit reminders and morning alerts, confirming that `setInexactRepeating` functions without `SecurityException`.

---

### Finding SEC-08: Dormant Third-Party Dependencies Expanding Attack Surface
- **Severity**: **Low**
- **OWASP Category**: M8: Security Misconfiguration
- **CWE**: CWE-1059: Incomplete Documentation / Unused Code Inclusion
- **Affected File & Line Numbers**:
  - `app/build.gradle.kts#L110, L120-L121, L124, L126, L128`
  - `metadata.json#L5`
  - `.env.example#L1-L8`

#### Evidence & Risk Explanation
`app/build.gradle.kts` bundles several heavyweight third-party libraries that are completely unreferenced in the Kotlin source code:
- `firebase-ai`
- `firebase-appcheck-recaptcha`
- `firebase-appcheck-debug`
- `retrofit`
- `converter-moshi`
- `okhttp`
- `logging-interceptor`

Additionally, `metadata.json` declares:
```json
"majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
```
And `.env.example` prompts for `GEMINI_API_KEY`.
Yet an exhaustive search across all Kotlin files reveals 0 lines of code initializing or calling Firebase, OkHttp, Retrofit, or Gemini AI. Bundling unused third-party dependencies unnecessarily inflates APK binary size and introduces potential supply-chain vulnerabilities from unmaintained transitive dependencies.

#### Realistic Attack Scenario
A high-severity CVE is disclosed in an older version of OkHttp or transitive networking libraries included in the app. Although HabitFlow does not execute network requests, automated mobile security scanners (e.g. MobSF) flag the binary as vulnerable, failing corporate or app store compliance audits.

#### Recommended Fix & Implementation
1. Comment out or remove unused dependencies from `app/build.gradle.kts` until active implementations are ready.
2. Update `metadata.json` to reflect actual active capabilities.

#### Verification Steps
1. Remove unused dependencies from `dependencies {}` in `app/build.gradle.kts`.
2. Execute `./gradlew assembleDebug` to confirm clean compilation.
3. Compare APK sizes to confirm reduction in binary footprint.

---

### Finding SEC-09: Lack of Input Sanitization and Max-Length Bounds on Habit & Category Creation
- **Severity**: **Low**
- **OWASP Category**: M7: Client Code Quality
- **CWE**: CWE-20: Improper Input Validation
- **Affected File & Line Numbers**:
  - `app/src/main/java/com/example/data/repository/HabitRepository.kt#L767-L775, L823-L829`
  - `app/src/main/java/com/example/data/repository/BackupRecoveryManager.kt#L144-L188`

#### Evidence & Risk Explanation
In `HabitRepository.kt`, inputs are checked for non-emptiness (`require(cleanTitle.isNotEmpty())`), but no length constraints or character sanitization are applied:
```kotlin
val cleanTitle = habit.title.trim()
require(cleanTitle.isNotEmpty()) { "Habit title cannot be empty." }
```
Similarly, when importing JSON backups in `BackupRecoveryManager.kt`, arbitrarily large strings or malformed unicode character blocks are inserted into SQLite without bounds checking. An imported JSON snapshot containing megabyte-sized strings can cause SQLite cursor row-window exceptions (`CursorWindowAllocationException`), Compose recomposition jank, or application out-of-memory crashes.

#### Realistic Attack Scenario
A user imports a crafted backup JSON containing a habit title with 500,000 characters. When rendering the `TodayScreen` or `HabitsListScreen`, Jetpack Compose text measurement exhausts the UI thread memory, resulting in persistent Application Not Responding (ANR) or crash loops on launch.

#### Recommended Fix & Implementation
Enforce strict length limits and character filtering on all user inputs and snapshot import pipelines:

```kotlin
// Example Input Validation Bounds
const val MAX_TITLE_LENGTH = 100
const val MAX_SUBTITLE_LENGTH = 200
const val MAX_CATEGORY_LENGTH = 50

fun validateHabitInput(title: String, subtitle: String, category: String) {
    require(title.isNotBlank() && title.length <= MAX_TITLE_LENGTH) {
        "Habit title must be between 1 and $MAX_TITLE_LENGTH characters."
    }
    require(subtitle.length <= MAX_SUBTITLE_LENGTH) {
        "Subtitle cannot exceed $MAX_SUBTITLE_LENGTH characters."
    }
    require(category.isNotBlank() && category.length <= MAX_CATEGORY_LENGTH) {
        "Category name must be between 1 and $MAX_CATEGORY_LENGTH characters."
    }
}
```

#### Verification Steps
1. Attempt to add or import a habit with a title exceeding 100 characters.
2. Confirm the UI gracefully displays a validation error without persisting or crashing.

---

### Finding SEC-10: Hardcoded Fallback Keystore Path in Release Signing Configuration
- **Severity**: **Low**
- **OWASP Category**: M8: Security Misconfiguration
- **CWE**: CWE-522: Insufficiently Protected Credentials
- **Affected File & Line Numbers**:
  - `app/build.gradle.kts#L29-L30`

#### Evidence & Risk Explanation
In `app/build.gradle.kts`, the release signing config defines:
```kotlin
val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
storeFile = file(keystorePath)
storePassword = System.getenv("STORE_PASSWORD")
keyAlias = "upload"
keyPassword = System.getenv("KEY_PASSWORD")
```
While `.gitignore` correctly ignores `*.jks` and `*.keystore`, relying on a fallback path in the project root directory creates an accidental commitment risk if a developer temporarily creates `my-upload-key.jks` and modifies `.gitignore`. Furthermore, if `KEYSTORE_PATH` is not set during local release builds, Gradle attempts to read an uncommitted file rather than failing fast with an informative error.

#### Realistic Attack Scenario
A developer generates `my-upload-key.jks` in the project root for local testing. While troubleshooting Git issues, the developer modifies `.gitignore` or runs `git add -f`, committing the private production signing key into Git history.

#### Recommended Fix & Implementation
Fail fast if release signing environment variables are missing during release builds:

```kotlin
signingConfigs {
    create("release") {
        val keystoreEnv = System.getenv("KEYSTORE_PATH")
        if (keystoreEnv != null) {
            storeFile = file(keystoreEnv)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

#### Verification Steps
1. Run `./gradlew assembleRelease` without `KEYSTORE_PATH` set.
2. Verify that Gradle halts with a descriptive error rather than looking for a local `.jks` file in the repository root.

---

## Areas Verified Secure & Non-Vulnerable

| Area | Status | Verification Context & Evidence |
| :--- | :--- | :--- |
| **Git History Secrets Scan** | **Verified Clean** | Audited Git commits from `da9f182` through `bf2b717`. No active API keys, JWTs, private keys, or keystores were committed. |
| **Broadcast Receiver Exposure**| **Verified Secure** | `HabitReminderReceiver` is explicitly declared with `android:exported="false"`, preventing other apps from triggering reminders. |
| **Local SQL Injection** | **Verified Secure** | `HabitDao.kt` exclusively utilizes compiled, parameterized Room `@Query` annotations (`:id`, `:title`, `:dateString`). No dynamic raw SQL string concatenations exist. |
| **Server-Side Injection (SSRF, CSRF, CORS)** | **Not Applicable** | The application currently operates completely offline with no listening ports, web servers, or external REST endpoints. |
| **Activity Hijacking** | **Standard** | `MainActivity` is the sole exported activity (`android:exported="true"`), acting strictly as the main app launcher (`android.intent.action.MAIN`). |

---

## Prioritized Remediation Checklist & Implementation Status

| Priority | Task ID | Remediation Action | Target Component | Status |
| :---: | :---: | :--- | :--- | :---: |
| **P0** | **REM-01** | Redact `syncKey` from JSON backup exports and hash passkeys with salted PBKDF2-HMAC-SHA256. | `BackupRecoveryManager.kt` | **Completed** |
| **P0** | **REM-02** | Disable full ADB backup (`android:allowBackup="false"`) and explicitly exclude database, sharedprefs, and recovery files. | `AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml` | **Completed** |
| **P1** | **REM-03** | Prepare clean `DatabaseSecurityConfig` hook, restrict destructive migration to legacy versions (`1, 2`), and enable WAL mode (full SQLCipher awaits external passphrase/dependency approval). | `AppDatabase.kt` | **Partially Completed** |
| **P1** | **REM-04** | Encrypt local backup mirrors and preferences at rest using Android Keystore (`AES/GCM/NoPadding` 256-bit key). | `BackupRecoveryManager.kt` | **Completed** |
| **P1** | **REM-05** | Enable R8 code shrinking, obfuscation (`isMinifyEnabled = true`), and resource shrinking (`isShrinkResources = true`) with Room/Compose keep rules for release builds. | `app/build.gradle.kts`, `app/proguard-rules.pro` | **Completed** |
| **P2** | **REM-06** | Replace misleading "Account Sync" UI with "Local Backup & Data Recovery" and enforce PBKDF2 passkey verification before profile restoration. | `ProfileScreen.kt`, `BackupRecoveryManager.kt` | **Completed** |
| **P2** | **REM-07** | Strip `syncKey` and sensitive passkey hashes from exported JSON snapshots and prevent plaintext passkey pre-population in UI fields. | `ProfileScreen.kt`, `BackupRecoveryManager.kt` | **Completed** |
| **P3** | **REM-08** | Remove unnecessary `SCHEDULE_EXACT_ALARM` permission from manifest. | `AndroidManifest.xml` | **Completed** |
| **P3** | **REM-09** | Enforce maximum length boundaries (`MAX_TITLE_LENGTH = 100`, `MAX_SUBTITLE_LENGTH = 200`, `MAX_CATEGORY_LENGTH = 50`) and 3–10 habit bounds on creation, updates, and snapshot imports. | `HabitRepository.kt`, `BackupRecoveryManager.kt` | **Completed** |
| **P3** | **REM-10** | Comment out dormant dependencies (`firebase-ai`, `firebase-appcheck-*`, `retrofit`, `okhttp`, `moshi`, `logging-interceptor`) in `app/build.gradle.kts` to minimize attack surface. | `app/build.gradle.kts` | **Completed** |

> **Platform Constraint Note (`SEC-08` & `SEC-10`)**: Per AI Studio platform build constraints, `signingConfigs` in `app/build.gradle.kts` and `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` in `metadata.json` are preserved unchanged for build container compatibility.

---

## Recommended Secure Database Access Configuration

When upgrading HabitFlow to full SQLCipher encryption or remote backend sync:

```kotlin
// Recommended Architecture for Secure Local Storage:
// 1. Hardware-backed AES-256-GCM SecretKey in AndroidKeyStore (Active in BackupRecoveryManager.kt)
// 2. Clean DatabaseSecurityConfig hook in AppDatabase.getInstance(context, securityConfig)
// 3. Optional SQLCipher SupportFactory can be attached to Room.databaseBuilder via DatabaseSecurityConfig
```

---

## Final Security Status & Verification Summary

| Metric | Status |
| :--- | :--- |
| **Overall Security Posture** | **Hardened (P0, P1, P2, P3 Remediations Applied)** |
| **Credential & Backup Protection** | `syncKey` excluded from JSON exports; passkeys hashed with salted PBKDF2-HMAC-SHA256; local mirror snapshots encrypted with Android Keystore AES-256-GCM; `android:allowBackup="false"` and strict XML exclusion rules active |
| **Release Binary Hardening** | R8 minification (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`) enabled with Room & Compose ProGuard keep rules; unused networking/Firebase libraries commented out |
| **Remaining Optional Enhancements** | Full SQLCipher database file encryption (`net.zetetic:sqlcipher-android`) and remote OAuth2 cloud backend when server credentials are provided |

