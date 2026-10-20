package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.signOutUser
import com.example.data.model.UserAccount

@Composable
fun RegistrationAndLoginGateScreen(
    userAccount: UserAccount?,
    userEmail: String?,
    isBootstrappedAdmin: Boolean,
    isSaving: Boolean,
    onRegisterSubmit: (
        phoneNumber: String,
        rawPassword: String
    ) -> Unit,
    onPhonePasswordLogin: (
        phoneNumber: String,
        rawPassword: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    // Default to Login (0) so Admin or existing users can immediately log in, or switch to Register (1)
    var selectedModeTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF073440),
                                        Color(0xFF0B4F60),
                                        Color(0xFF146B7E)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Apartment,
                                        contentDescription = null,
                                        tint = Color(0xFF073440),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "ইমারত পরিদর্শনের মাসিক রিপোর্ট",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!userEmail.isNullOrBlank()) {
                                        Text(
                                            text = "সংযুক্ত: $userEmail",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFDE68A)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "ফোন নাম্বার ও পাসওয়ার্ড লগইন / রেজিস্ট্রেশন",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ফোন নাম্বার ও পাসওয়ার্ড দিয়ে রেজিস্ট্রেশন করুন। অ্যাডমিন অনুমোদন দিলে লগইন করে আপনার নাম সেট করতে পারবেন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Mode Tabs: লগইন (Login) | রেজিস্ট্রেশন (Registration)
                    TabRow(
                        selectedTabIndex = selectedModeTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Tab(
                            selected = selectedModeTab == 0,
                            onClick = { selectedModeTab = 0 },
                            text = { Text("লগইন (Login)", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
                            modifier = Modifier.testTag("tab_phone_login")
                        )
                        Tab(
                            selected = selectedModeTab == 1,
                            onClick = { selectedModeTab = 1 },
                            text = { Text("রেজিস্ট্রেশন (Register)", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                            modifier = Modifier.testTag("tab_phone_register")
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (selectedModeTab == 0) {
                            // LOGIN TAB CONTENT
                            PhoneLoginContent(
                                userAccount = userAccount,
                                isSaving = isSaving,
                                onSwitchToRegister = { selectedModeTab = 1 },
                                onPhonePasswordLogin = onPhonePasswordLogin
                            )
                        } else {
                            // REGISTRATION TAB CONTENT
                            PhoneRegistrationContent(
                                existingAccount = userAccount,
                                isBootstrappedAdmin = isBootstrappedAdmin,
                                isSaving = isSaving,
                                onRegisterSubmit = onRegisterSubmit,
                                onSwitchToLogin = { selectedModeTab = 0 }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneLoginContent(
    userAccount: UserAccount?,
    isSaving: Boolean,
    onSwitchToRegister: () -> Unit,
    onPhonePasswordLogin: (
        phoneNumber: String,
        rawPassword: String,
        onError: (String) -> Unit
    ) -> Unit
) {
    var phoneInput by rememberSaveable(userAccount?.phoneNumber) {
        mutableStateOf(userAccount?.phoneNumber ?: "")
    }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Status banner based on user's current registration state
    when (userAccount?.approvalStatus) {
        "pending" -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_approval_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "অ্যাডমিন অনুমোদনের অপেক্ষায় (Pending Approval)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "নিবন্ধিত ফোন: ${userAccount.phoneNumber}। অ্যাডমিন অনুমোদন দিলে নিচে লগইন করতে পারবেন। (অ্যাডমিন লগইনের জন্য সরাসরি নিচে ফোন ও পাসওয়ার্ড দিন)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        "rejected" -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rejected_approval_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "রেজিস্ট্রেশন আবেদন অনুমোদিত হয়নি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Text(
                        text = "অ্যাডমিন আপনার আবেদনটি বাতিল করেছেন। পুনরায় আবেদন করতে রেজিস্ট্রেশন ট্যাবে যান।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        "approved" -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "অ্যাডমিন কর্তৃক অনুমোদিত (Approved)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "আপনার রেজিস্ট্রেশন সম্পন্ন হয়েছে। ফোন নাম্বার ও পাসওয়ার্ড দিয়ে লগইন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        else -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ফোন নাম্বার ও পাসওয়ার্ড দিয়ে লগইন করুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নতুন ব্যবহারকারী হলে 'রেজিস্ট্রেশন' ট্যাবে গিয়ে ফোন নাম্বার ও পাসওয়ার্ড দিয়ে নিবন্ধন করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (loginError != null) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = loginError ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    OutlinedTextField(
        value = phoneInput,
        onValueChange = {
            val filtered = it.filter { ch -> ch.isDigit() || ch in "+-() " }.take(20)
            phoneInput = filtered
            loginError = null
        },
        label = { Text("ফোন নাম্বার (Phone Number) *") },
        placeholder = { Text("017XXXXXXXX") },
        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_phone_input")
    )

    OutlinedTextField(
        value = passwordInput,
        onValueChange = {
            passwordInput = it
            loginError = null
        },
        label = { Text("পাসওয়ার্ড (Password) *") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "পাসওয়ার্ড দেখুন"
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input")
    )

    Button(
        onClick = {
            when {
                phoneInput.isBlank() -> loginError = "অনুগ্রহ করে ফোন নাম্বার লিখুন"
                passwordInput.isBlank() -> loginError = "অনুগ্রহ করে পাসওয়ার্ড লিখুন"
                else -> {
                    loginError = null
                    onPhonePasswordLogin(phoneInput, passwordInput) { errMsg ->
                        loginError = errMsg
                    }
                }
            }
        },
        enabled = !isSaving,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("phone_password_login_button")
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "লগইন করুন (Login)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (userAccount == null) {
        TextButton(
            onClick = onSwitchToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("go_to_register_button")
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("অ্যাকাউন্ট নেই? নতুন রেজিস্ট্রেশন করুন", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PhoneRegistrationContent(
    existingAccount: UserAccount?,
    isBootstrappedAdmin: Boolean,
    isSaving: Boolean,
    onRegisterSubmit: (
        phoneNumber: String,
        rawPassword: String
    ) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var phoneNumber by rememberSaveable(existingAccount?.phoneNumber) {
        mutableStateOf(existingAccount?.phoneNumber ?: "")
    }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "শুধুমাত্র ফোন নাম্বার ও পাসওয়ার্ড দিয়ে রেজিস্ট্রেশন করুন। অ্যাডমিন অনুমোদন দিলে লগইন করার পর আপনি আপনার নাম সেট করে নিতে পারবেন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (existingAccount != null && existingAccount.approvalStatus == "approved") {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "আপনার রেজিস্ট্রেশন ইতিমধ্যে অনুমোদিত। আপনি লগইন ট্যাব থেকে প্রবেশ করতে পারেন অথবা নিচে পাসওয়ার্ড হালনাগাদ করতে পারেন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }

    if (errorText != null) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = errorText ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    OutlinedTextField(
        value = phoneNumber,
        onValueChange = {
            val filtered = it.filter { ch -> ch.isDigit() || ch in "+-() " }.take(20)
            phoneNumber = filtered
            errorText = null
        },
        label = { Text("ফোন নাম্বার (রেজিস্ট্রেশন ও লগইনের জন্য) *") },
        placeholder = { Text("017XXXXXXXX") },
        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reg_phone_input")
    )

    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            errorText = null
        },
        label = { Text("পাসওয়ার্ড (কমপক্ষে ৪ অক্ষর) *") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "পাসওয়ার্ড দেখুন"
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reg_password_input")
    )

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = {
            confirmPassword = it
            errorText = null
        },
        label = { Text("পাসওয়ার্ড নিশ্চিত করুন (Confirm Password) *") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reg_confirm_password_input")
    )

    Button(
        onClick = {
            val cleanPhone = phoneNumber.trim()
            when {
                cleanPhone.length < 5 || !cleanPhone.matches(Regex("^[0-9+\\-() ]+$")) -> {
                    errorText = "অনুগ্রহ করে সঠিক ফোন নাম্বার লিখুন (কমপক্ষে ৫ সংখ্যা)"
                }
                password.trim().length < 4 -> {
                    errorText = "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"
                }
                password.trim() != confirmPassword.trim() -> {
                    errorText = "উভয় পাসওয়ার্ড মেলেনি"
                }
                else -> {
                    errorText = null
                    onRegisterSubmit(
                        cleanPhone,
                        password.trim()
                    )
                    if (!isBootstrappedAdmin) {
                        onSwitchToLogin()
                    }
                }
            }
        },
        enabled = !isSaving,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("submit_registration_button")
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (existingAccount == null) "ফোন ও পাসওয়ার্ড দিয়ে রেজিস্ট্রেশন করুন" else "পাসওয়ার্ড হালনাগাদ করুন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PostLoginSetNameScreen(
    phoneNumber: String,
    initialDesignation: String,
    initialOfficeName: String,
    isAdmin: Boolean,
    isSaving: Boolean,
    onSaveName: (inspectorName: String, designation: String, officeName: String) -> Unit
) {
    var inspectorName by rememberSaveable { mutableStateOf("") }
    var designation by rememberSaveable(initialDesignation) {
        mutableStateOf(
            initialDesignation.ifBlank {
                if (isAdmin) "অ্যাডমিন / অথরাইজড অফিসার" else "ইমারত পরিদর্শক"
            }
        )
    }
    var officeName by rememberSaveable(initialOfficeName) { mutableStateOf(initialOfficeName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Text(
                        text = "আপনার নাম সেট করুন",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "লগইন সফল হয়েছে (ফোন: $phoneNumber)। মাসিক রিপোর্ট তৈরি ও সংরক্ষণের জন্য আপনার নাম নির্ধারণ করে নিন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = {
                            inspectorName = it
                            errorMessage = null
                        },
                        label = { Text("আপনার নাম / ইমারত পরিদর্শকের নাম *") },
                        placeholder = { Text("যেমন: প্রকৌশলী মোঃ রাশেদুল ইসলাম") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_login_set_name_input")
                    )

                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("পদবী / জোন (ঐচ্ছিক)") },
                        placeholder = { Text("যেমন: ইমারত পরিদর্শক, জোন-৪") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_login_designation_input")
                    )

                    OutlinedTextField(
                        value = officeName,
                        onValueChange = { officeName = it },
                        label = { Text("অফিস / কর্তৃপক্ষের নাম (ঐচ্ছিক)") },
                        placeholder = { Text("যেমন: রাজধানী উন্নয়ন কর্তৃপক্ষ (রাজউক)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_login_office_input")
                    )

                    Button(
                        onClick = {
                            if (inspectorName.trim().isEmpty()) {
                                errorMessage = "অনুগ্রহ করে আপনার নাম লিখুন"
                            } else {
                                onSaveName(
                                    inspectorName.trim(),
                                    designation.trim(),
                                    officeName.trim()
                                )
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("post_login_save_name_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "নাম সেট করে অ্যাপে প্রবেশ করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
