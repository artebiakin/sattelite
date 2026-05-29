package nz.satellite.smsdemo.data.source

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.satellite.smsdemo.domain.model.SmsMessage
import nz.satellite.smsdemo.domain.model.SmsType

/** Reads SMS from the system content provider and maps cursor rows to domain models. */
class SmsContentDataSource(private val context: Context) {

    suspend fun loadMessages(limit: Int): List<SmsMessage> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ,
        )

        val messages = mutableListOf<SmsMessage>()
        context.contentResolver.query(
            Uri.parse("content://sms"),
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC LIMIT $limit",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                messages.add(
                    SmsMessage(
                        id = cursor.getLong(idIdx),
                        address = cursor.getString(addrIdx) ?: "",
                        body = cursor.getString(bodyIdx) ?: "",
                        dateMs = cursor.getLong(dateIdx),
                        type = cursor.getInt(typeIdx).toSmsType(),
                        read = cursor.getInt(readIdx) == 1,
                    )
                )
            }
        }
        messages
    }

    private fun Int.toSmsType(): SmsType = when (this) {
        Telephony.Sms.MESSAGE_TYPE_INBOX -> SmsType.Inbox
        Telephony.Sms.MESSAGE_TYPE_SENT -> SmsType.Sent
        Telephony.Sms.MESSAGE_TYPE_DRAFT -> SmsType.Draft
        else -> SmsType.Other
    }
}
