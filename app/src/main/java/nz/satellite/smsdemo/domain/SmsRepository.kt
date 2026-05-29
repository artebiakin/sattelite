package nz.satellite.smsdemo.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import nz.satellite.smsdemo.domain.model.SatelliteState
import nz.satellite.smsdemo.domain.model.SmsMessage
import nz.satellite.smsdemo.domain.model.SmsSendEvent

/**
 * Single source of truth for SMS and satellite state. The UI layer depends only
 * on this interface; the concrete implementation lives in the data layer and is
 * the only place that knows about ContentResolver, SmsManager, and TelephonyManager.
 */
interface SmsRepository {

    /** Stream of send/delivery events produced by [sendSms]. */
    val sendEvents: Flow<SmsSendEvent>

    /** Current satellite / non-terrestrial network state. */
    val satelliteState: StateFlow<SatelliteState>

    /** Sends an SMS, emitting progress on [sendEvents]. */
    fun sendSms(recipient: String, message: String)

    /** Loads recent messages from the device (inbox + sent). */
    suspend fun loadMessages(limit: Int = 100): List<SmsMessage>

    /** Begins observing the satellite network state. */
    fun startSatelliteMonitoring()

    /** Stops observing the satellite network state. */
    fun stopSatelliteMonitoring()
}
