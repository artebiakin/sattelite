package nz.satellite.smsdemo.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import nz.satellite.smsdemo.data.source.SatelliteStatusDataSource
import nz.satellite.smsdemo.data.source.SmsContentDataSource
import nz.satellite.smsdemo.data.source.SmsSenderDataSource
import nz.satellite.smsdemo.domain.SmsRepository
import nz.satellite.smsdemo.domain.model.SatelliteState
import nz.satellite.smsdemo.domain.model.SmsMessage
import nz.satellite.smsdemo.domain.model.SmsSendEvent

/**
 * Default [SmsRepository], coordinating the three data sources. This is the only
 * class that the data sources are wired through, keeping framework details out of
 * the domain and UI layers.
 */
class SmsRepositoryImpl(context: Context) : SmsRepository {

    private val sender = SmsSenderDataSource(context)
    private val content = SmsContentDataSource(context)
    private val satellite = SatelliteStatusDataSource(context)

    override val sendEvents: Flow<SmsSendEvent> = sender.events
    override val satelliteState: StateFlow<SatelliteState> = satellite.state

    override fun sendSms(recipient: String, message: String) = sender.send(recipient, message)

    override suspend fun loadMessages(limit: Int): List<SmsMessage> = content.loadMessages(limit)

    override fun startSatelliteMonitoring() = satellite.start()

    override fun stopSatelliteMonitoring() = satellite.stop()
}
