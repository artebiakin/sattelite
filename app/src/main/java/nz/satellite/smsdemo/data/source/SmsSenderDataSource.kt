package nz.satellite.smsdemo.data.source

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import nz.satellite.smsdemo.data.smsResultDescription
import nz.satellite.smsdemo.domain.model.SmsSendEvent
import java.util.concurrent.atomic.AtomicInteger

/** Wraps [SmsManager] and surfaces sent/delivered callbacks as a [Flow]. */
class SmsSenderDataSource(private val context: Context) {

    private val _events = Channel<SmsSendEvent>(capacity = Channel.UNLIMITED)
    val events: Flow<SmsSendEvent> = _events.receiveAsFlow()

    private val messageCounter = AtomicInteger(0)

    fun send(recipient: String, message: String) {
        val manager = smsManager()
        val parts = manager.divideMessage(message)
        val msgId = messageCounter.incrementAndGet()

        val sentPendingIntents = ArrayList<PendingIntent>(parts.size)
        val deliveredPendingIntents = ArrayList<PendingIntent>(parts.size)

        for (i in parts.indices) {
            val sentAction = "${ACTION_SENT}_${msgId}_$i"
            val deliveredAction = "${ACTION_DELIVERED}_${msgId}_$i"

            val sentIntent = Intent(sentAction).setPackage(context.packageName)
            val deliveredIntent = Intent(deliveredAction).setPackage(context.packageName)

            val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            sentPendingIntents.add(PendingIntent.getBroadcast(context, 0, sentIntent, flags))
            deliveredPendingIntents.add(
                PendingIntent.getBroadcast(context, 0, deliveredIntent, flags)
            )

            val receiverFlags = if (Build.VERSION.SDK_INT >= 33) Context.RECEIVER_NOT_EXPORTED else 0
            val partIndex = i

            context.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        context.unregisterReceiver(this)
                        _events.trySend(
                            SmsSendEvent.Sent(
                                recipient = recipient,
                                partIndex = partIndex,
                                success = resultCode == Activity.RESULT_OK,
                                description = smsResultDescription(resultCode),
                            )
                        )
                    }
                },
                IntentFilter(sentAction),
                receiverFlags,
            )

            context.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context?, intent: Intent?) {
                        context.unregisterReceiver(this)
                        _events.trySend(SmsSendEvent.Delivered(recipient, partIndex))
                    }
                },
                IntentFilter(deliveredAction),
                receiverFlags,
            )
        }

        if (parts.size == 1) {
            manager.sendTextMessage(
                recipient, null, parts[0], sentPendingIntents[0], deliveredPendingIntents[0],
            )
        } else {
            manager.sendMultipartTextMessage(
                recipient, null, parts, sentPendingIntents, deliveredPendingIntents,
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun smsManager(): SmsManager =
        if (Build.VERSION.SDK_INT >= 31) context.getSystemService(SmsManager::class.java)
        else SmsManager.getDefault()

    private companion object {
        const val ACTION_SENT = "nz.satellite.smsdemo.SMS_SENT"
        const val ACTION_DELIVERED = "nz.satellite.smsdemo.SMS_DELIVERED"
    }
}
