<img src="app/src/main/assets/potential_logo.png" alt="Potential. For European Digital Identity. Co-funded by the European Union."  style="width: 400px;"/>
Funded by the European Union. Views and opinions expressed are however those of the author(s) only and do not 
necessarily reflect those of the European Union or Potential Consortium. Neither the European Union nor the granting 
authority can be held responsible for them.

# EE Wallet PoC

Proof of Concept EE Digital Identity Wallet application for Android.

The solution supports remote presentation flows for Personal Identification Data (PID) in SD-JWT and mDOC format and
Mobile Driving License (mDL) in mDOC format.

The issuance flow is based on
the [OpenID for Verifiable Credential Issuance Draft 14](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0-14.html)
and the presentation flow is based on the
[OpenID for Verifiable Presentations Draft 22](https://openid.net/specs/openid-4-verifiable-presentations-1_0-22.html).

## Testing using pre-built package

1. Download and install
   the [wallet application (the APK can be found under Assets)](https://github.com/open-eid/eudi-wallet-poc/releases).
2. Open the application and follow the instructions to create a new wallet and issue a Personal Identification Data
   (PID) and Mobile Driving License (mDL).
   credential. [Demo (link to instruction video)](https://github.com/user-attachments/assets/2ad34855-f81b-4595-8bc2-ed438670835e)
3. Go to the online Verifier service https://verifier.eudiw.dev.
   > NB! This verifier service is independent of this project and may not be available or may not be compatible at the
   time of your testing.
4. Insert the following [certificate](app/src/main/assets/iaca_root.cer.pem) using the `Configure issuer chain`
   menu. [Demo (link to instruction video)](https://github.com/user-attachments/assets/2e0a8cf7-c951-4bd0-8a83-05fc5fce8962)
5. Select either or both `PID` and `mDL` credentials to
   verify. [Demo (link to instruction video)](https://github.com/user-attachments/assets/7e757d80-ee34-47e8-9435-db4cdbe86056)
6. Open the wallet link (or scan the generated QR code if its cross-device
   flow). [Demo (link to instruction video)](https://github.com/user-attachments/assets/70bf9f31-11c2-4342-8e44-55ec099af6c9)
7. Follow the instructions in the wallet application to present the requested
   credentials. [Demo (link to instruction video)](https://github.com/user-attachments/assets/f7183cd5-667d-42ac-a69d-bb20ec3bd080)
8. Verify the presented credentials in the online Verifier service.

## Testing with other verifiers

It is possible to test with any verifier as long as a compatible protocol and credential formats are used and required
certificates are trusted by wallet and verifier.

1. The verifier certificate in PEM format must be added to [trusted.pem](/app/src/main/res/raw/trusted.pem).
2. The APK must be built according to the instructions below.
3. The wallet [certificate](app/src/main/assets/iaca_root.cer.pem) must be trusted by the verifier.

For example, you can set up your own verifier using the
[reference implementation verifier service](https://github.com/eu-digital-identity-wallet/eudi-srv-web-verifier-endpoint-23220-4-kt?tab=readme-ov-file#run-all-verifier-components-together).

> NB! This verifier implementation is independent of this project and appropriate version needs to be used, supporting
> the OpenID for Verifiable Presentations Draft 22+.

## Prerequisites

* JDK 17+
* [Android Studio](https://developer.android.com/studio) or [IntelliJ IDEA](https://www.jetbrains.com/idea) with
  [Android](https://plugins.jetbrains.com/plugin/22989-android) plugin
* Android SDK API 35

If you build from a command line be sure to have `ANDROID_HOME` env variable pointing to Android SDK folder.

## Quick-Start

For the development it is recommended to use IDE to run gradle tasks. The Android emulator in IDE can also be used to
run the app locally.

Alternatively, tasks can be run from the command line:

* `./gradlew build` - builds and assembles debug and release APKs
* `./gradlew assembleDebug` - builds and assembles only debug APK
* `./gradlew test` - run tests

Assembled APKs can be found here: `build/outputs/apk/`

> The app uses `local_mocks` as the default build variant. This build variant is used to run the app with all the
> backend services mocked. This is useful for development and testing purposes.
