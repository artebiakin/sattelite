package nz.satellite.smsdemo

import android.app.Activity
import android.telephony.SmsManager

/**
 * Maps SmsManager result codes to human-readable strings.
 * Pure function — no Android framework calls — so it can be JVM unit-tested.
 */
fun smsResultDescription(resultCode: Int): String = when (resultCode) {
    Activity.RESULT_OK -> "Sent successfully"
    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "Generic failure"
    SmsManager.RESULT_ERROR_NO_SERVICE -> "No service (no network or satellite coverage)"
    SmsManager.RESULT_ERROR_NULL_PDU -> "Null PDU (internal error)"
    SmsManager.RESULT_ERROR_RADIO_OFF -> "Radio is off (airplane mode?)"
    SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> "Rate limit exceeded — slow down"
    SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE -> "Fixed-dialing-number check failed"
    SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED -> "Short code not allowed"
    SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED -> "Short code never allowed"
    SmsManager.RESULT_RIL_CANCELLED -> "Cancelled by RIL"
    SmsManager.RESULT_RIL_SMS_SEND_FAIL_RETRY -> "Send failed — retrying"
    SmsManager.RESULT_RIL_NETWORK_REJECT -> "Network rejected the message"
    SmsManager.RESULT_RIL_INVALID_STATE -> "Invalid modem state"
    SmsManager.RESULT_RIL_INVALID_SMSC_ADDRESS -> "Invalid SMSC address"
    SmsManager.RESULT_RIL_INTERNAL_ERR -> "RIL internal error"
    SmsManager.RESULT_RIL_REQUEST_NOT_SUPPORTED -> "Request not supported by modem"
    SmsManager.RESULT_RIL_INVALID_ARGUMENTS -> "Invalid arguments"
    SmsManager.RESULT_RIL_INVALID_SIM_STATE -> "Invalid SIM state"
    SmsManager.RESULT_RIL_NO_MEMORY -> "Modem out of memory"
    SmsManager.RESULT_RIL_REQUEST_RATE_LIMITED -> "Request rate limited by modem"
    SmsManager.RESULT_RIL_SIMULTANEOUS_SMS_AND_CALL_NOT_ALLOWED ->
        "Simultaneous SMS and call not allowed"
    SmsManager.RESULT_RIL_SIM_ABSENT -> "SIM absent"
    SmsManager.RESULT_UNEXPECTED_EVENT_STOP_SENDING -> "Unexpected event — send stopped"
    SmsManager.RESULT_NO_DEFAULT_SMS_APP -> "No default SMS app set"
    else -> "Unknown result code: $resultCode"
}
