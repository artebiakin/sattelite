package nz.satellite.smsdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nz.satellite.smsdemo.SmsDemoApplication
import nz.satellite.smsdemo.domain.SmsRepository
import nz.satellite.smsdemo.domain.model.SatelliteState
import nz.satellite.smsdemo.domain.model.SmsMessage
import nz.satellite.smsdemo.domain.model.SmsSendEvent
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** A line in the on-screen status log. */
data class LogEntry(val time: String, val message: String)

/** Immutable UI state rendered by the screen. */
data class UiState(
    val recipient: String = "",
    val messageText: String = "",
    val smsSendPermissionGranted: Boolean = false,
    val phoneStatePermissionGranted: Boolean = false,
    val readSmsPermissionGranted: Boolean = false,
    val log: List<LogEntry> = emptyList(),
    val isSending: Boolean = false,
    val smsList: List<SmsMessage> = emptyList(),
    val isSmsListLoading: Boolean = false,
)

class SmsViewModel(private val repository: SmsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    val satelliteState: StateFlow<SatelliteState> = repository.satelliteState

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        viewModelScope.launch {
            repository.sendEvents.collect { event -> onSendEvent(event) }
        }
    }

    private fun onSendEvent(event: SmsSendEvent) {
        val entry = when (event) {
            is SmsSendEvent.Sent -> {
                _uiState.update { it.copy(isSending = false) }
                if (event.success) {
                    refreshSmsList()
                    LogEntry(now(), "SENT to ${event.recipient} (part ${event.partIndex + 1}): ${event.description}")
                } else {
                    LogEntry(now(), "SEND FAILED to ${event.recipient}: ${event.description}")
                }
            }
            is SmsSendEvent.Delivered ->
                LogEntry(now(), "DELIVERED to ${event.recipient} (part ${event.partIndex + 1})")
        }
        _uiState.update { it.copy(log = listOf(entry) + it.log) }
    }

    fun onRecipientChange(value: String) = _uiState.update { it.copy(recipient = value) }
    fun onMessageChange(value: String) = _uiState.update { it.copy(messageText = value) }

    fun onPermissionsResult(smsGranted: Boolean, phoneGranted: Boolean, readSmsGranted: Boolean) {
        _uiState.update {
            it.copy(
                smsSendPermissionGranted = smsGranted,
                phoneStatePermissionGranted = phoneGranted,
                readSmsPermissionGranted = readSmsGranted,
            )
        }
        if (phoneGranted) repository.startSatelliteMonitoring()
        if (readSmsGranted) refreshSmsList()
    }

    fun refreshSmsList() {
        if (!_uiState.value.readSmsPermissionGranted) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSmsListLoading = true) }
            val messages = repository.loadMessages()
            _uiState.update { it.copy(smsList = messages, isSmsListLoading = false) }
        }
    }

    fun sendSms() {
        val state = _uiState.value
        if (!state.smsSendPermissionGranted) return
        if (state.recipient.isBlank() || state.messageText.isBlank()) return
        _uiState.update { it.copy(isSending = true) }
        val entry = LogEntry(now(), "Sending to ${state.recipient}…")
        _uiState.update { it.copy(log = listOf(entry) + it.log) }
        repository.sendSms(state.recipient.trim(), state.messageText)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopSatelliteMonitoring()
    }

    private fun now(): String = LocalTime.now().format(timeFormatter)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SmsDemoApplication
                SmsViewModel(app.smsRepository)
            }
        }
    }
}
