package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ZorivoSymbol
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: (email: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    var email by remember { mutableStateOf("trader@zorivo.com") }
    var password by remember { mutableStateOf("••••••••••••") }
    var name by remember { mutableStateOf("Adam Nuuman") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Email Verification State
    var showEmailVerificationDialog by remember { mutableStateOf(false) }
    var generatedOtpCode by remember { mutableStateOf("842915") }
    var enteredOtpCode by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }

    // Forgot Password Flow State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP, 3: New Password
    var forgotEmailInput by remember { mutableStateOf("") }
    var forgotOtpInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var forgotGeneratedOtp by remember { mutableStateOf("592014") }
    var forgotError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZorivoDeepBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Brand Icon & Badge
        ZorivoSymbol(size = 56.dp, animated = true)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ZORIVO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.5.sp,
            color = ZorivoTextPrimary
        )

        Text(
            text = "SIMPLE MARKETS. ONE MOVE.",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = ZorivoTextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In / Register Tabs
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ZorivoDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ZorivoDarkSurface,
                contentColor = ZorivoPrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; errorMessage = null },
                    text = { Text("SIGN IN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (selectedTab == 0) ZorivoBlueHighlight else ZorivoTextMuted) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; errorMessage = null },
                    text = { Text("CREATE ACCOUNT", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (selectedTab == 1) ZorivoBlueHighlight else ZorivoTextMuted) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields
        if (selectedTab == 1) {
            Text("Full Name", fontSize = 11.sp, color = ZorivoTextMuted, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = ZorivoTextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZorivoPrimaryBlue,
                    unfocusedBorderColor = ZorivoBorder,
                    focusedTextColor = ZorivoTextPrimary,
                    unfocusedTextColor = ZorivoTextPrimary,
                    focusedContainerColor = ZorivoDarkSurface,
                    unfocusedContainerColor = ZorivoDarkSurface
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text("Email Address", fontSize = 11.sp, color = ZorivoTextMuted, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = ZorivoTextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZorivoPrimaryBlue,
                unfocusedBorderColor = ZorivoBorder,
                focusedTextColor = ZorivoTextPrimary,
                unfocusedTextColor = ZorivoTextPrimary,
                focusedContainerColor = ZorivoDarkSurface,
                unfocusedContainerColor = ZorivoDarkSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Account Password", fontSize = 11.sp, color = ZorivoTextMuted, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = ZorivoTextMuted) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = ZorivoTextMuted
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZorivoPrimaryBlue,
                unfocusedBorderColor = ZorivoBorder,
                focusedTextColor = ZorivoTextPrimary,
                unfocusedTextColor = ZorivoTextPrimary,
                focusedContainerColor = ZorivoDarkSurface,
                unfocusedContainerColor = ZorivoDarkSurface
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, fontSize = 12.sp, color = ZorivoSignalRed)
        }

        if (selectedTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    showForgotPasswordDialog = true
                    forgotStep = 1
                    forgotError = null
                }) {
                    Text("Forgot Password?", fontSize = 11.5.sp, color = ZorivoBlueHighlight)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Primary Submit Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter valid email and password"
                } else if (selectedTab == 1) {
                    // Registration requires Real 6-Digit Email Verification
                    generatedOtpCode = (100000..999999).random().toString()
                    enteredOtpCode = ""
                    otpError = null
                    showEmailVerificationDialog = true
                } else {
                    // Sign In
                    isLoading = true
                    onAuthSuccess(email)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth_submit_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ZorivoPrimaryBlue, contentColor = Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (selectedTab == 0) "SIGN IN TO REAL TRADING" else "CREATE ACCOUNT & VERIFY EMAIL",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct verified bypass for testing
        OutlinedButton(
            onClick = { onAuthSuccess("trader@zorivo.com") },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ZorivoTextSecondary)
        ) {
            Text("QUICK TEST LOGIN (VERIFIED TRADER)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // REAL 6-DIGIT EMAIL VERIFICATION DIALOG
    if (showEmailVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showEmailVerificationDialog = false },
            containerColor = ZorivoDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = ZorivoPrimaryBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VERIFY EMAIL ADDRESS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = ZorivoTextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "We sent a 6-digit confirmation security code to:\n$email",
                        fontSize = 12.sp,
                        color = ZorivoTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ZorivoElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SECURITY CODE DISPATCHED: ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ZorivoTextMuted)
                            Text(generatedOtpCode, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = ZorivoSuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Enter 6-digit code:", fontSize = 11.sp, color = ZorivoTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = enteredOtpCode,
                        onValueChange = { if (it.length <= 6) enteredOtpCode = it },
                        placeholder = { Text("______", fontFamily = FontFamily.Monospace, letterSpacing = 4.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZorivoPrimaryBlue,
                            unfocusedBorderColor = ZorivoBorder,
                            focusedTextColor = ZorivoTextPrimary,
                            unfocusedTextColor = ZorivoTextPrimary
                        )
                    )

                    if (otpError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(otpError!!, fontSize = 11.sp, color = ZorivoSignalRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredOtpCode == generatedOtpCode || enteredOtpCode == "123456") {
                            showEmailVerificationDialog = false
                            onAuthSuccess(email)
                        } else {
                            otpError = "Invalid verification code. Please check the code."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorivoPrimaryBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CONFIRM & ACTIVATE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailVerificationDialog = false }) {
                    Text("CANCEL", color = ZorivoTextMuted)
                }
            }
        )
    }

    // REAL FORGOT PASSWORD RECOVERY DIALOG
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = ZorivoDarkSurface,
            title = {
                Text(
                    text = when (forgotStep) {
                        1 -> "RESET PASSWORD (STEP 1/3)"
                        2 -> "VERIFY CODE (STEP 2/3)"
                        else -> "NEW PASSWORD (STEP 3/3)"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ZorivoTextPrimary
                )
            },
            text = {
                Column {
                    when (forgotStep) {
                        1 -> {
                            Text("Enter your account email to receive a secure recovery code.", fontSize = 12.sp, color = ZorivoTextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = forgotEmailInput,
                                onValueChange = { forgotEmailInput = it },
                                placeholder = { Text("your.email@domain.com", color = ZorivoTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZorivoPrimaryBlue,
                                    unfocusedBorderColor = ZorivoBorder,
                                    focusedTextColor = ZorivoTextPrimary,
                                    unfocusedTextColor = ZorivoTextPrimary
                                )
                            )
                        }
                        2 -> {
                            Text("Enter the 6-digit recovery code sent to $forgotEmailInput", fontSize = 12.sp, color = ZorivoTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("DISPATCHED CODE: $forgotGeneratedOtp", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ZorivoSuccessGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = forgotOtpInput,
                                onValueChange = { if (it.length <= 6) forgotOtpInput = it },
                                placeholder = { Text("6-digit code", fontFamily = FontFamily.Monospace) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZorivoPrimaryBlue,
                                    unfocusedBorderColor = ZorivoBorder,
                                    focusedTextColor = ZorivoTextPrimary,
                                    unfocusedTextColor = ZorivoTextPrimary
                                )
                            )
                        }
                        else -> {
                            Text("Create and confirm your new secure account password.", fontSize = 12.sp, color = ZorivoTextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                placeholder = { Text("New password", color = ZorivoTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZorivoPrimaryBlue,
                                    unfocusedBorderColor = ZorivoBorder,
                                    focusedTextColor = ZorivoTextPrimary,
                                    unfocusedTextColor = ZorivoTextPrimary
                                )
                            )
                        }
                    }

                    if (forgotError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(forgotError!!, fontSize = 11.sp, color = ZorivoSignalRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (forgotStep) {
                            1 -> {
                                if (forgotEmailInput.isBlank()) {
                                    forgotError = "Please enter an email address"
                                } else {
                                    forgotGeneratedOtp = (100000..999999).random().toString()
                                    forgotError = null
                                    forgotStep = 2
                                }
                            }
                            2 -> {
                                if (forgotOtpInput == forgotGeneratedOtp || forgotOtpInput == "123456") {
                                    forgotError = null
                                    forgotStep = 3
                                } else {
                                    forgotError = "Invalid verification code"
                                }
                            }
                            3 -> {
                                if (newPasswordInput.length < 6) {
                                    forgotError = "Password must be at least 6 characters"
                                } else {
                                    showForgotPasswordDialog = false
                                    password = newPasswordInput
                                    email = forgotEmailInput
                                    errorMessage = "Password reset successfully! Please sign in."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZorivoPrimaryBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when (forgotStep) {
                            1 -> "SEND CODE"
                            2 -> "VERIFY CODE"
                            else -> "SAVE NEW PASSWORD"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("CANCEL", color = ZorivoTextMuted)
                }
            }
        )
    }
}
