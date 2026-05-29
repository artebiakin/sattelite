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
import nz.satellite.smsdemo.ui.SmsViewModel
import nz.satellite.smsdemo.ui.theme.SatelliteSmsDemoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SmsViewModel by viewModels { SmsViewModel.Factory }

    private val requestedPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS,
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        viewModel.onPermissionsResult(
            smsGranted = results[Manifest.permission.SEND_SMS] == true,
            phoneGranted = results[Manifest.permission.READ_PHONE_STATE] == true,
            readSmsGranted = results[Manifest.permission.READ_SMS] == true,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionLauncher.launch(requestedPermissions)

        setContent {
            SatelliteSmsDemoTheme {
                val uiState by viewModel.uiState.collectAsState()
                val satelliteState by viewModel.satelliteState.collectAsState()

                SmsDemoScreen(
                    uiState = uiState,
                    satelliteState = satelliteState,
                    onRecipientChange = viewModel::onRecipientChange,
                    onMessageChange = viewModel::onMessageChange,
                    onSend = viewModel::sendSms,
                    onRefreshMessages = viewModel::refreshSmsList,
                    onRequestPermissions = { permissionLauncher.launch(requestedPermissions) },
                )
            }
        }
    }
}
