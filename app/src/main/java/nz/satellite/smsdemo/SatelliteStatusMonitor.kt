package nz.satellite.smsdemo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.ServiceState
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

enum class SatelliteState {
    /** Phone is currently on a non-terrestrial (satellite) network. */
    Satellite,

    /** Phone is on a normal terrestrial cellular network. */
    Terrestrial,

    /**
     * State is unknown — either READ_PHONE_STATE permission is missing,
     * the device is on API < 35, or there is no service at all.
     */
    Unknown,
}

class SatelliteStatusMonitor(private val context: Context) {

    private val _state = MutableStateFlow(SatelliteState.Unknown)
    val state: StateFlow<SatelliteState> = _state

    // Held so we can unregister on cleanup.
    private var callback: TelephonyCallback? = null

    fun start() {
        if (Build.VERSION.SDK_INT < 35) {
            // isUsingNonTerrestrialNetwork() requires API 35 (Android 15).
            _state.value = SatelliteState.Unknown
            return
        }
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _state.value = SatelliteState.Unknown
            return
        }

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val cb = object : TelephonyCallback(), TelephonyCallback.ServiceStateListener {
            override fun onServiceStateChanged(serviceState: ServiceState) {
                _state.value = if (serviceState.isUsingNonTerrestrialNetwork) {
                    SatelliteState.Satellite
                } else {
                    SatelliteState.Terrestrial
                }
            }
        }
        callback = cb
        telephonyManager.registerTelephonyCallback(Executors.newSingleThreadExecutor(), cb)
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < 31) return
        val cb = callback ?: return
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.unregisterTelephonyCallback(cb)
        callback = null
    }
}
