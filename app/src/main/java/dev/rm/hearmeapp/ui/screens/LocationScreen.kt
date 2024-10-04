package dev.rm.hearmeapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

import dev.rm.hearmeapp.vm.AuthViewModel
import dev.rm.hearmeapp.vm.LocationState
import dev.rm.hearmeapp.vm.LocationViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationState by locationViewModel.locationState.collectAsState()
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var showDialog by remember { mutableStateOf(false) }
    val (username, email) = authViewModel.getCurrentUserInfo()
    val words = username?.split(' ')
    val name =
        words?.joinToString(separator = "_") { word -> word.replaceFirstChar { it.uppercase() } }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        locationViewModel.requestLocationPermissions(permissionsState)

        if (permissionsState.allPermissionsGranted) {
            locationViewModel.initializeFusedLocationClient(context)
            locationViewModel.getCurrentLocation()
        }
    }

    LaunchedEffect(locationState) {
        when (locationState) {
            is LocationState.Success -> {
                // Optional: You can show a toast or handle success
            }

            is LocationState.Error -> {
                Toast.makeText(
                    context,
                    (locationState as LocationState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗣️HearMe",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.secondary

                )

                Button(onClick = {
                    navController.navigate("home")
                }) {
                    Text(text = "🏠")
                }

                Button(onClick = {
                    navController.navigate("messages")
                }) {
                    Text("📝")
                }

                Button(
                    onClick = {
                        vibrator.vibrate(100) // Vibrate for 100 milliseconds
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This is your actual location, ${name}!",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = locationState) {
                    is LocationState.Initial -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Waiting for location...")
                        }
                    }

                    is LocationState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is LocationState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message)
                        }
                    }

                    is LocationState.Success -> {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(state.location, 15f)
                        }
                        val markerState = rememberMarkerState(position = state.location)

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                        ) {
                            Marker(
                                state = markerState,
                                title = "You are here",
                                snippet = "Current location",
                                alpha = 1.0f,
                                anchor = Offset(0.5f, 1.0f),
                                flat = false,
                                rotation = 0.0f
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                val shareText = locationViewModel.getShareableLocation()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share Location"
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 16.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Location",
                                tint = Color.Gray
                            )
                        }
                    }
                }

            }

        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Logout") },
            text = {
                Text(
                    "Are you sure you want to log out?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        showDialog = false
                    }
                ) {
                    Text(
                        "Yes", style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        "No", style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        )
    }
}

