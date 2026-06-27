package com.example.voyagers.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.voyagers.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TripViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val contacts by viewModel.contacts.collectAsState()
    val uiError by viewModel.uiError.collectAsState()

    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    // Permission States
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var smsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Launchers for permissions request
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationGranted = isGranted
        Toast.makeText(context, if (isGranted) "GPS Permission Granted" else "GPS Permission Denied", Toast.LENGTH_SHORT).show()
    }

    val smsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        smsGranted = isGranted
        Toast.makeText(context, if (isGranted) "SMS Permission Granted" else "SMS Permission Denied", Toast.LENGTH_SHORT).show()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationGranted = isGranted
        Toast.makeText(context, if (isGranted) "Notifications Permission Granted" else "Notifications Permission Denied", Toast.LENGTH_SHORT).show()
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF130F26), Color(0xFF0F0B1E))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SETTINGS & GUARDIANS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Display Banner
            AnimatedVisibility(
                visible = uiError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiError?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4C4C).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMsg,
                                color = Color(0xFFFF6B6B),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Dismiss", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Permissions Check Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00FFD1), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Security Permissions Status",
                            color = Color(0xFF00FFD1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // GPS Location Permission Row
                    PermissionRow(
                        title = "GPS Location Permission",
                        description = "Required to track your coordinates along the journey",
                        isGranted = locationGranted,
                        onRequest = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // SMS Permission Row
                    PermissionRow(
                        title = "SMS Dispatch Permission",
                        description = "Required to send automatic SOS text updates to your contacts",
                        isGranted = smsGranted,
                        onRequest = { smsLauncher.launch(Manifest.permission.SEND_SMS) }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Notification Permission Row
                        PermissionRow(
                            title = "Notifications Permission",
                            description = "Required to run the background service and display checkpoints",
                            isGranted = notificationGranted,
                            onRequest = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                        )
                    }
                }
            }

            // Manage Contacts Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, tint = Color(0xFF00FFD1), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency Guardians Management",
                            color = Color(0xFF00FFD1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Contact inline form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Name", color = Color.LightGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFD1),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone", color = Color.LightGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFD1),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val success = viewModel.addContact(newName, newPhone)
                                if (success) {
                                    newName = ""
                                    newPhone = ""
                                    focusManager.clearFocus()
                                    Toast.makeText(context, "Contact Saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF00FFD1),
                                contentColor = Color(0xFF130F26)
                            ),
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact List
                    if (contacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No contacts configured yet.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        contacts.forEach { contact ->
                            ListItem(
                                headlineContent = { Text(contact.name, color = Color.White, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(contact.phoneNumber, color = Color.LightGray) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF00FFD1)
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.deleteContact(contact) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Contact",
                                            tint = Color(0xFFFF5252)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.White.copy(alpha = 0.02f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            // SMS Dispatch History Log Card
            val smsLogs by viewModel.smsLogs.collectAsState()
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Voyage SMS Alert History (${smsLogs.size})",
                            color = Color(0xFF00FFD1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (smsLogs.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearSmsLogs() }) {
                                Text("Clear", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (smsLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No alerts dispatched yet.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        smsLogs.forEach { log ->
                            ListItem(
                                headlineContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.contactName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(
                                            text = if (log.isSimulated) "SIMULATED" else "SENT",
                                            color = if (log.isSimulated) Color(0xFF00FFD1) else Color(0xFF8BC34A),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                supportingContent = {
                                    Column {
                                        Text(log.messageText, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                                        Text(
                                            text = java.text.SimpleDateFormat("hh:mm:ss a - dd MMM", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.White.copy(alpha = 0.01f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = Color.LightGray, fontSize = 11.sp, lineHeight = 14.sp)
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color(0xFF00FFD1).copy(alpha = 0.2f),
                    disabledContentColor = Color(0xFF00FFD1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Granted", fontSize = 12.sp)
                }
            }
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Request", fontSize = 12.sp)
                }
            }
        }
    }
}
