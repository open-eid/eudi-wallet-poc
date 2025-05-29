package ee.cyber.wallet

import android.os.Build

object AppConfig {

    val appVersion by lazy { "${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}" }

    val userAgent by lazy { "${BuildConfig.CLIENT_USER_AGENT}/$appVersion (Android SDK ${Build.VERSION.SDK_INT}; ${Build.DEVICE})" }

    val deepLinkSchema by lazy { BuildConfig.DEEP_LINK_SCHEMA }
    val deepLinkPresentation by lazy { BuildConfig.PRESENTATION_REDIRECT_URI }
    val deepLinkIssuance by lazy { BuildConfig.ISSUE_REDIRECT_URI }

    val pidProviderUrl by lazy { BuildConfig.PID_PROVIDER_URL }
    val walletProviderRpcUrl by lazy { BuildConfig.WALLET_PROVIDER_RPC_URL }
    val pidProviderRpcUrl by lazy { BuildConfig.PID_PROVIDER_RPC_URL }

    val clientId by lazy { BuildConfig.CLIENT_ID }

    val clientConnectTimeoutSeconds by lazy { BuildConfig.CLIENT_CONNECT_TIMEOUT_SECONDS }
    val clientReadTimeoutSeconds by lazy { BuildConfig.CLIENT_READ_TIMEOUT_SECONDS }
    val clientWriteTimeoutSeconds by lazy { BuildConfig.CLIENT_WRITE_TIMEOUT_SECONDS }
    val issuerUrl by lazy { BuildConfig.ISSUER_URL }
    val clientLogLevel by lazy { BuildConfig.CLIENT_LOG_LEVEL }
    val useMocks by lazy { BuildConfig.USE_MOCKS }
}
