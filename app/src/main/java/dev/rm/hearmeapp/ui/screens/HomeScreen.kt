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
import com.google.accompanist.permissions.rememberMultiplePermissionsState

import dev.rm.hearmeapp.vm.AuthState
import dev.rm.hearmeapp.vm.AuthViewModel
import dev.rm.hearmeapp.vm.MessageState
import dev.rm.hearmeapp.vm.MessageViewModel
import dev.rm.hearmeapp.vm.SpeechRecognitionViewModel
import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import dev.rm.hearmeapp.vm.SpeechState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    messageViewModel: MessageViewModel = viewModel(),
    speechRecognitionViewModel: SpeechRecognitionViewModel = viewModel()
) {

    val authState = authViewModel.authState.observeAsState()
//    val messageState by messageViewModel.messageState.observeAsState()
    val messageState by messageViewModel.messageState.collectAsState()
    val speechState by speechRecognitionViewModel.speechState.collectAsState()

    var userInput by remember { mutableStateOf(TextFieldValue()) }
    var selectedMessage by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var messageSource by remember { mutableStateOf<MessageSource?>(null) }


    val (username, email) = authViewModel.getCurrentUserInfo()
    val words = username?.split(' ');
    val name =
        words?.joinToString(separator = "_") { word -> word.replaceFirstChar { it.uppercase() } }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


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

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

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

    LaunchedEffect(speechState) {
        when (speechState) {
            is SpeechState.Success -> {
//                Toast.makeText(context, (speechState as SpeechState.Success).recognizedText, Toast.LENGTH_SHORT).show()
//                messageViewModel.saveMessage(speechState.recognizedText)
            }

            is SpeechState.Error -> {
                Toast.makeText(
                    context,
                    (speechState as SpeechState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Header(navController, vibrator) {
            showDialog.value = true
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome back, ${name}!",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val tabTitles = listOf("Write", "Speech")

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                WriteTab(
                    userInput,
                    { userInput = it },
                    {
                        saveMessage(messageViewModel, userInput.text)
                        selectedMessage = userInput.text
                        messageSource = MessageSource.WRITE
                    },
                    { message ->
                        selectedMessage = message
                        messageSource = MessageSource.WRITE
                    }
                )
            }

            1 -> {
                SpeechTab(
                    speechRecognitionViewModel,
                    permissionsState,
                    { message ->
                        selectedMessage = message
                        messageSource = MessageSource.SPEECH
                    },
                    { message ->
                        saveMessage(messageViewModel, message)
                    }
                )
            }
        }

        if (selectedMessage.isNotEmpty()) {
            MessageDisplayBox(message = selectedMessage, source = messageSource)
        }

        Spacer(modifier = Modifier.height(24.dp))

        DefaultMessages { message ->
            selectedMessage = message
            messageSource = MessageSource.WRITE
        }



        LogoutDialog(showDialog) { authViewModel.logout() }
    }
}


@Composable
fun WriteTab(
    userInput: TextFieldValue,
    onUserInputChange: (TextFieldValue) -> Unit,
    onSaveMessage: () -> Unit,
    onDefaultMessageSelected: (String) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = userInput,
            onValueChange = onUserInputChange,
            label = { Text("Write your message") },
            placeholder = { Text("Enter your message here") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Message input field" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSaveMessage()
                onUserInputChange(TextFieldValue(""))
                onDefaultMessageSelected(userInput.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                "Show and Save Message",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechTab(
    speechRecognitionViewModel: SpeechRecognitionViewModel,
    permissionsState: MultiplePermissionsState,
    onDefaultMessageSelected: (String) -> Unit,
    onSaveMessage: (String) -> Unit
) {
    val speechState by speechRecognitionViewModel.speechState.collectAsState()
    var isMessageProcessed by remember { mutableStateOf(false) }

    Column {
        Spacer(modifier = Modifier.height(24.dp))

        if (!permissionsState.allPermissionsGranted) {
            Text("Microphone permission is required.")
            return
        }

        Button(
            onClick = {
                if (speechState is SpeechState.Loading) {
                    speechRecognitionViewModel.stopListening()
                } else {
                    speechRecognitionViewModel.startListening()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                if (speechState is SpeechState.Loading) "Stop Listening" else "Start Listening",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
        }

        when (speechState) {
            is SpeechState.Loading -> {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            is SpeechState.Success -> {
                val recognizedText = (speechState as SpeechState.Success).recognizedText
                onDefaultMessageSelected(recognizedText)
                if (!isMessageProcessed) {
                    onDefaultMessageSelected(recognizedText)
                    onSaveMessage(recognizedText)
                    isMessageProcessed = true
                }
            }

            is SpeechState.Error -> {
                onDefaultMessageSelected((speechState as SpeechState.Error).message)
                isMessageProcessed = false
            }

            else -> {
                isMessageProcessed = false
            }
        }
    }
}


@Composable
fun DefaultMessages(onMessageSelected: (String) -> Unit) {
    val defaultMessages = listOf(
        "Hello! 😊",
        "How are you? 🤔",
        "Thank you! 🙏",
        "Please help! 🆘",
        "I need assistance. 🆘"
    )

    LazyColumn {
        items(defaultMessages) { message ->
            Button(
                onClick = { onMessageSelected(message) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)

            ) {
                Text(message)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LogoutDialog(showDialog: MutableState<Boolean>, onConfirm: () -> Unit) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?",  style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        showDialog.value = false
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
                    onClick = { showDialog.value = false },
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
fun Header(
    navController: NavController,
    vibrator: Vibrator,
    onLogoutClick: () -> Unit
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
            navController.navigate("messages")
        }) {
            Text("📝")
        }

        Button(
            onClick = {
                vibrator.vibrate(100)
                onLogoutClick()
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
}

@Composable
fun MessageDisplayBox(
    message: String,
    modifier: Modifier = Modifier,
    source: MessageSource? = null
) {
    Box(
        modifier = modifier
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
            .semantics {
                contentDescription = "Displayed message: $message"
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when (source) {
                    MessageSource.WRITE -> "📝 Written Message:"
                    MessageSource.SPEECH -> "🗣️ Spoken Message:"
                    null -> ""
                },
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = message,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray
                )
            )
        }
    }
}

private fun saveMessage(messageViewModel: MessageViewModel, message: String) {
    messageViewModel.saveMessage(message)
}

enum class MessageSource {
    WRITE,
    SPEECH
}
