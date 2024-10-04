package dev.rm.hearmeapp.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.rm.hearmeapp.vm.AuthViewModel
import dev.rm.hearmeapp.vm.SpeechRecognitionViewModel
import dev.rm.hearmeapp.vm.SpeechState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechRecognitionScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    speechRecognitionViewModel: SpeechRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val speechState by speechRecognitionViewModel.speechState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.RECORD_AUDIO)
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            speechRecognitionViewModel.initializeSpeechRecognizer(context)
        }
    }

    LaunchedEffect(speechState) {
        when (speechState) {
            is SpeechState.Success -> {
//                Toast.makeText(context, (speechState as SpeechState.Success).recognizedText, Toast.LENGTH_SHORT).show()
            }

            is SpeechState.Error -> {
                Toast.makeText(context, (speechState as SpeechState.Error).message, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🗣️Speech Recognition",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Button(onClick = { speechRecognitionViewModel.startListening() }) {
                Text(text = "Start Listening")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (speechState) {
                is SpeechState.Loading -> {
                    CircularProgressIndicator()
                }
                is SpeechState.Error -> {
                    Text(text = (speechState as SpeechState.Error).message)
                }
                is SpeechState.Success -> {
                    Text(text = "Recognized: ${(speechState as SpeechState.Success).recognizedText}")
                }
                else -> {
                    Text(text = "Press the button to start")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to proceed?") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
