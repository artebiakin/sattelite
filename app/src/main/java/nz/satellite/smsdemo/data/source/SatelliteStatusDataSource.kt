package nz.satellite.smsdemo.data.source

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.ServiceState
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nz.satellite.smsdemo.domain.model.SatelliteState
import java.util.concurrent.Executors

/** Observes whether the device is on a non-terrestrial (satellite) network. */
class SatelliteStatusDataSource(private val context: Context) {

    private val _state = MutableStateFlow(SatelliteState.Unknown)
    val state: StateFlow<SatelliteState> = _state

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

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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
        tm.registerTelephonyCallback(Executors.newSingleThreadExecutor(), cb)
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < 31) return
        val cb = callback ?: return
        (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            .unregisterTelephonyCallback(cb)
        callback = null
    }
}
