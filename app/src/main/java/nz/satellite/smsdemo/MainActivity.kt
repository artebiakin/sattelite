package nz.satellite.smsdemo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import nz.satellite.smsdemo.ui.SmsDemoScreen
import nz.satellite.smsdemo.ui.theme.SatelliteSmsDemoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SmsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val smsGranted = results[Manifest.permission.SEND_SMS] == true
        val phoneGranted = results[Manifest.permission.READ_PHONE_STATE] == true
        val readSmsGranted = results[Manifest.permission.READ_SMS] == true
        viewModel.onPermissionsResult(smsGranted, phoneGranted, readSmsGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request both permissions on first launch.
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_SMS,
            ),
        )

        setContent {
            SatelliteSmsDemoTheme {
                val uiState by viewModel.uiState.collectAsState()
                val satelliteState by viewModel.satelliteMonitor.state.collectAsState()

                SmsDemoScreen(
                    uiState = uiState,
                    satelliteState = satelliteState,
                    onRecipientChange = viewModel::onRecipientChange,
                    onMessageChange = viewModel::onMessageChange,
                    onSend = viewModel::sendSms,
                    onRefreshMessages = viewModel::refreshSmsList,
                    onRequestPermissions = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_SMS,
                            ),
                        )
                    },
                )
            }
        }
    }
}
