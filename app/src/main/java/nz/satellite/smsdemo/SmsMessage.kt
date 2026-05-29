package nz.satellite.smsdemo

data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val dateMs: Long,
    val type: SmsType,
    val read: Boolean,
)

enum class SmsType { Inbox, Sent, Draft, Other }
