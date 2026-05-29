package nz.satellite.smsdemo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class LogEntry(val time: String, val message: String)

data class UiState(
    val recipient: String = "",
    val messageText: String = "",
    val smsSendPermissionGranted: Boolean = false,
    val phoneStatePermissionGranted: Boolean = false,
    val log: List<LogEntry> = emptyList(),
    val isSending: Boolean = false,
)

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val sender = SmsSender(application)
    val satelliteMonitor = SatelliteStatusMonitor(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        viewModelScope.launch {
            sender.events.collect { event ->
                val entry = when (event) {
                    is SmsEvent.Sent -> {
                        _uiState.update { it.copy(isSending = false) }
                        val desc = smsResultDescription(event.resultCode)
                        if (event.resultCode == android.app.Activity.RESULT_OK) {
                            LogEntry(now(), "SENT to ${event.recipient} (part ${event.partIndex + 1}): $desc")
                        } else {
                            LogEntry(now(), "SEND FAILED to ${event.recipient}: $desc")
                        }
                    }
                    is SmsEvent.Delivered ->
                        LogEntry(now(), "DELIVERED to ${event.recipient} (part ${event.partIndex + 1})")
                }
                _uiState.update { it.copy(log = listOf(entry) + it.log) }
            }
        }
    }

    fun onRecipientChange(value: String) = _uiState.update { it.copy(recipient = value) }
    fun onMessageChange(value: String) = _uiState.update { it.copy(messageText = value) }

    fun onPermissionsResult(smsGranted: Boolean, phoneGranted: Boolean) {
        _uiState.update {
            it.copy(
                smsSendPermissionGranted = smsGranted,
                phoneStatePermissionGranted = phoneGranted,
            )
        }
        if (phoneGranted) satelliteMonitor.start()
    }

    fun sendSms() {
        val state = _uiState.value
        if (!state.smsSendPermissionGranted) return
        if (state.recipient.isBlank() || state.messageText.isBlank()) return
        _uiState.update { it.copy(isSending = true) }
        val entry = LogEntry(now(), "Sending to ${state.recipient}…")
        _uiState.update { it.copy(log = listOf(entry) + it.log) }
        sender.send(state.recipient.trim(), state.messageText)
    }

    override fun onCleared() {
        super.onCleared()
        satelliteMonitor.stop()
    }

    private fun now(): String = LocalTime.now().format(timeFormatter)
}
