package nz.satellite.smsdemo.domain.model

/**
 * Result of attempting to send an SMS part. The data layer is responsible for
 * translating Android result codes into [success] + [description] so the rest
 * of the app never sees framework constants.
 */
sealed interface SmsSendEvent {
    data class Sent(
        val recipient: String,
        val partIndex: Int,
        val success: Boolean,
        val description: String,
    ) : SmsSendEvent

    data class Delivered(
        val recipient: String,
        val partIndex: Int,
    ) : SmsSendEvent
}
