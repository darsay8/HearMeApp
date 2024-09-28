package dev.rm.hearmeapp.ui.screens

import android.content.Context
import android.os.Vibrator
import android.widget.Toast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import dev.rm.hearmeapp.data.model.Message
import dev.rm.hearmeapp.vm.AuthState
import dev.rm.hearmeapp.vm.AuthViewModel
import dev.rm.hearmeapp.vm.MessageState
import dev.rm.hearmeapp.vm.MessageViewModel

import java.text.SimpleDateFormat
import java.util.Locale



@Composable
fun MessagesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: MessageViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val messageState by viewModel.messageState.collectAsState()
//    val messageState by viewModel.messageState.observeAsState()
    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var showDialog by remember { mutableStateOf(false) }
    val (username, email) = authViewModel.getCurrentUserInfo()
    val words = username?.split(' ')
    val name =
        words?.joinToString(separator = "_") { word -> word.replaceFirstChar { it.uppercase() } }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT)
                    .show()
            }

            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getMessages()
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
                    navController.navigate("location")
                }) {
                    Text("📍")
                }

                Button(onClick = {
                    navController.navigate("home")
                }) {
                    Text(text = "🏠")
                }

                Button(onClick = {
                    vibrator.vibrate(100) // Vibrate for 100 milliseconds
                    showDialog = true
                }) {
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
                text = "These are your messages, ${name}!",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(32.dp))


            when (messageState) {
                is MessageState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                is MessageState.Error -> {
                    Text(
                        text = (messageState as MessageState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    if (messages.isEmpty()) {
                        Text("No messages available.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(messages) { message ->
                                MessageItem(
                                    message = message,
                                    onDelete = { viewModel.deleteMessage(message.id) }
                                )
                            }
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

@Composable
fun MessageItem(
    message: Message,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm" , Locale.getDefault())
    val formattedDate = dateFormat.format(message.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "$formattedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
