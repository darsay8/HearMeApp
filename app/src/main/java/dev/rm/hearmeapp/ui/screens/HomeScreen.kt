package dev.rm.hearmeapp.ui.screens

import android.content.Context
import android.os.Vibrator
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import dev.rm.hearmeapp.vm.AuthState
import dev.rm.hearmeapp.vm.AuthViewModel
import dev.rm.hearmeapp.vm.MessageState
import dev.rm.hearmeapp.vm.MessageViewModel


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    messageViewModel: MessageViewModel = viewModel()
) {

    val authState = authViewModel.authState.observeAsState()
    val messageState by messageViewModel.messageState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    var userInput by remember { mutableStateOf(TextFieldValue()) }
    var selectedMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val (username, email) = authViewModel.getCurrentUserInfo()
    val words = username?.split(' ');
    val name =
        words?.joinToString(separator = "_") { word -> word.replaceFirstChar { it.uppercase() } }


    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val defaultMessages = listOf(
        "Hello! 😊",
        "How are you? 🤔",
        "Thank you! 🙏",
        "Please help! 🆘",
        "I need assistance. 🆘"
    )

    LaunchedEffect(messageState) {
        when (messageState) {
            is MessageState.Loading -> {
                // Optionally show loading state
            }

            is MessageState.SavedToDB -> {
                Toast.makeText(context, "Message saved!", Toast.LENGTH_SHORT).show()
//                userInput = TextFieldValue("")
            }

            is MessageState.Error -> {
                val errorMessage = (messageState as MessageState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }


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
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 10.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome back, ${name}!",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = userInput,
            onValueChange = { newValue -> userInput = newValue },
            label = { Text("Write your message") },
            placeholder = { Text("Enter your message here") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Message input field" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedMessage = userInput.text
                messageViewModel.saveMessage(userInput.text)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .semantics { contentDescription = "Show message button" }
        ) {
            Text(
                "Show Message", style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (selectedMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp)
                    .border(
                        2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(32.dp)
                    .semantics { contentDescription = "Displayed message: $selectedMessage" }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🗣️ Message:",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = selectedMessage,
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.DarkGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)

        ) {
            items(defaultMessages) { message ->
                Button(
                    onClick = { selectedMessage = message },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .semantics { contentDescription = "Select $message" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = message, style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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

}

