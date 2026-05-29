package nz.satellite.smsdemo

import android.content.Context
import android.net.Uri
import android.provider.Telephony

class SmsRepository(private val context: Context) {

    fun loadMessages(limit: Int = 100): List<SmsMessage> {
        val uri = Uri.parse("content://sms")
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
            uri,
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
                        type = when (cursor.getInt(typeIdx)) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> SmsType.Inbox
                            Telephony.Sms.MESSAGE_TYPE_SENT -> SmsType.Sent
                            Telephony.Sms.MESSAGE_TYPE_DRAFT -> SmsType.Draft
                            else -> SmsType.Other
                        },
                        read = cursor.getInt(readIdx) == 1,
                    )
                )
            }
        }
        return messages
    }
}
