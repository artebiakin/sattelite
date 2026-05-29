package nz.satellite.smsdemo.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nz.satellite.smsdemo.LogEntry
import nz.satellite.smsdemo.R
import nz.satellite.smsdemo.SatelliteState
import nz.satellite.smsdemo.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDemoScreen(
    uiState: UiState,
    satelliteState: SatelliteState,
    onRecipientChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onRequestPermissions: () -> Unit,
    onRefreshMessages: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SatelliteBanner(satelliteState)

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Send") })
                Tab(selected = selectedTab == 1, onClick = {
                    selectedTab = 1
                    onRefreshMessages()
                }, text = { Text("Messages") })
            }

            when (selectedTab) {
                0 -> SendTab(
                    uiState = uiState,
                    onRecipientChange = onRecipientChange,
                    onMessageChange = onMessageChange,
                    onSend = onSend,
                    onRequestPermissions = onRequestPermissions,
                )
                1 -> SmsListScreen(
                    messages = uiState.smsList,
                    isLoading = uiState.isSmsListLoading,
                    hasPermission = uiState.readSmsPermissionGranted,
                    onRefresh = onRefreshMessages,
                    onRequestPermission = onRequestPermissions,
                )
            }
        }
    }
}

@Composable
private fun SendTab(
    uiState: UiState,
    onRecipientChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        if (!uiState.smsSendPermissionGranted) {
            PermissionBanner(onRequestPermissions)
        } else {
            SendForm(
                uiState = uiState,
                onRecipientChange = onRecipientChange,
                onMessageChange = onMessageChange,
                onSend = onSend,
            )
        }

        Spacer(Modifier.height(12.dp))
        StatusLog(uiState.log)
        Spacer(Modifier.height(12.dp))
        HelpCard()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SatelliteBanner(state: SatelliteState) {
    val (bgColor, icon, text) = when (state) {
        SatelliteState.Satellite -> Triple(
            Color(0xFF1B5E20), "📡", stringResource(R.string.sat_status_satellite),
        )
        SatelliteState.Terrestrial -> Triple(
            Color(0xFF0D47A1), "📶", stringResource(R.string.sat_status_terrestrial),
        )
        SatelliteState.Unknown -> Triple(
            Color(0xFF37474F), "❓", stringResource(R.string.sat_status_unknown),
        )
    }
    Surface(modifier = Modifier.fillMaxWidth(), color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(icon, fontSize = 16.sp)
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PermissionBanner(onRequest: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "SMS permission is required to send messages.",
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Button(onClick = onRequest) {
                Text(stringResource(R.string.btn_grant_permissions))
            }
        }
    }
}

@Composable
private fun SendForm(
    uiState: UiState,
    onRecipientChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val charCount = uiState.messageText.length
    val segments = ((charCount - 1) / 160 + 1).coerceAtLeast(1)
    val canSend = uiState.recipient.isNotBlank() &&
        uiState.messageText.isNotBlank() &&
        !uiState.isSending

    OutlinedTextField(
        value = uiState.recipient,
        onValueChange = onRecipientChange,
        label = { Text(stringResource(R.string.label_recipient)) },
        placeholder = { Text(stringResource(R.string.hint_recipient)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.messageText,
        onValueChange = onMessageChange,
        label = { Text(stringResource(R.string.label_message)) },
        placeholder = { Text(stringResource(R.string.hint_message)) },
        minLines = 3,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text(
                stringResource(R.string.chars_remaining, charCount, 1, segments),
                style = MaterialTheme.typography.labelSmall,
            )
        },
    )
    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onSend, enabled = canSend) {
            Text(stringResource(R.string.btn_send))
        }
        if (uiState.isSending) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun StatusLog(log: List<LogEntry>) {
    Text(
        stringResource(R.string.log_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(4.dp))
    if (log.isEmpty()) {
        Text(
            stringResource(R.string.log_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(log) { entry ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        entry.time,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        entry.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpCard() {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.help_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.help_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
