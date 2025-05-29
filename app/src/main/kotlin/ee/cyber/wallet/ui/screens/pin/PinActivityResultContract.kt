package ee.cyber.wallet.ui.screens.pin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import kotlinx.parcelize.Parcelize

enum class PinFlow {
    CONFIRM_PRESENTATION,
    CONFIRM_ISSUANCE,
    CREATE
}

@Parcelize
data class Input(
    val flow: PinFlow = PinFlow.CONFIRM_PRESENTATION,
    val party: String? = null
) : Parcelable

sealed class Result(val resultCode: Int) {
    data class Success(val pin: String) : Result(Activity.RESULT_OK)
    data object Failure : Result(1)
    data object Cancelled : Result(Activity.RESULT_CANCELED)
}

class PinActivityResultContract : ActivityResultContract<Input, Result>() {

    override fun createIntent(context: Context, input: Input): Intent = Intent(context, PinActivity::class.java).apply { putExtra(EXTRA_INPUT, input) }

    override fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
        Activity.RESULT_OK -> Result.Success(intent?.getStringExtra(EXTRA_PIN)!!)
        1 -> Result.Failure
        else -> Result.Cancelled
    }

    companion object {
        const val EXTRA_PIN = "pin"
        const val EXTRA_INPUT = "input"
    }
}
