package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsSheet(
    userProfile: UserProfile,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onUpdateProfile: (name: String, age: String, dob: String, phone: String, email: String, address: String) -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onUpdateTheme: (bgColor: String?, selectionColor: String?, fileColor: String?) -> Unit
) {
    val context = LocalContext.current

    var name by remember(userProfile) { mutableStateOf(userProfile.fullName) }
    var age by remember(userProfile) { mutableStateOf(userProfile.age) }
    var dob by remember(userProfile) { mutableStateOf(userProfile.dob) }
    var phone by remember(userProfile) { mutableStateOf(userProfile.phoneNumber) }
    var email by remember(userProfile) { mutableStateOf(userProfile.email) }
    var address by remember(userProfile) { mutableStateOf(userProfile.address) }

    // PIN Management Dialog States
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "EzWallet Menu",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Profile, Security & App Info",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_menu_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4: CUSTOM THEME
            ThemeSettingsSection(
                currentBgColor = userProfile.customBgColor,
                currentSelectionColor = userProfile.customSelectionColor,
                currentFileColor = userProfile.customFileColor,
                onUpdateTheme = onUpdateTheme
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: ABOUT DEVELOPER & APP
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "About Developer & App",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailItemRow(label = "App Name", value = "EzWallet - Digital Vault")
                    DetailItemRow(label = "Developer Name", value = "Abhirup Das")
                    DetailItemRow(label = "Contact Developer", value = "abhiripd476@gmail.com")
                    DetailItemRow(label = "App Version", value = "v2.6.3 (Encrypted Local Storage)")

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "EzWallet guarantees encrypted on-device storage. If you remove files from your device gallery, your digital copy remains safely stored here.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: APPLOCK (PIN) SECURITY
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userProfile.isPinSet) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (userProfile.isPinSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AppLock (PIN Protection)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (userProfile.isPinSet) "Active • PIN required on app open & delete" else "Disabled • Tap to set 4-digit PIN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProfile.isPinSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = userProfile.isPinSet,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    showSetPinDialog = true
                                } else {
                                    showDisablePinDialog = true
                                }
                            },
                            modifier = Modifier.testTag("switch_applock_pin")
                        )
                    }

                    if (userProfile.isPinSet) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showChangePinDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_change_pin")
                            ) {
                                Text("Change PIN")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showDisablePinDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("btn_turn_off_pin")
                            ) {
                                Text("Turn Off Lock")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 3: EDITABLE USER PERSONAL DETAILS
            Text(
                text = "My Personal Credentials",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Save and edit your personal details for instant digital access.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Age & Date of Birth Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_profile_age")
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("input_profile_dob")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_phone")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mail ID
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Mail ID") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_email")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_profile_address")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Personal Details Button
            Button(
                onClick = {
                    onUpdateProfile(name, age, dob, phone, email, address)
                    Toast.makeText(context, "Personal details saved successfully!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_profile")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Personal Credentials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        PinInputDialog(
            title = "Set AppLock PIN",
            subtitle = "Enter 4-digit PIN to secure your documents",
            onDismiss = { showSetPinDialog = false },
            onConfirm = { pin ->
                if (pin.length == 4) {
                    onSetPin(pin)
                    showSetPinDialog = false
                    Toast.makeText(context, "AppLock PIN activated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please enter 4 digits", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Disable PIN Dialog
    if (showDisablePinDialog) {
        PinInputDialog(
            title = "Turn Off AppLock",
            subtitle = "Enter current 4-digit PIN to disable security lock",
            onDismiss = { showDisablePinDialog = false },
            onConfirm = { pin ->
                if (pin == userProfile.pinCode) {
                    onDisablePin()
                    showDisablePinDialog = false
                    Toast.makeText(context, "AppLock PIN turned off", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var step by remember { mutableStateOf(1) }
        var oldPinVerified by remember { mutableStateOf(false) }

        if (step == 1) {
            PinInputDialog(
                title = "Verify Current PIN",
                subtitle = "Enter your current 4-digit PIN",
                onDismiss = { showChangePinDialog = false },
                onConfirm = { pin ->
                    if (pin == userProfile.pinCode) {
                        oldPinVerified = true
                        step = 2
                    } else {
                        Toast.makeText(context, "Incorrect current PIN", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            PinInputDialog(
                title = "Enter New 4-digit PIN",
                subtitle = "Set your new security PIN",
                onDismiss = { showChangePinDialog = false },
                onConfirm = { newPin ->
                    if (newPin.length == 4) {
                        onSetPin(newPin)
                        showChangePinDialog = false
                        Toast.makeText(context, "PIN successfully updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter 4 digits", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
