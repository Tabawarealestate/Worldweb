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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ReferralProfile
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalDarkBg
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceHighlight
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import com.example.ui.theme.ZorivoBlueHighlight
import com.example.ui.theme.ZorivoPrimaryBlue
import com.example.ui.theme.ZorivoTextMuted
import com.example.ui.theme.ZorivoTextPrimary

@Composable
fun ProfileScreen(
    onOpenAdmin: () -> Unit = {}
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var copiedCodeToast by remember { mutableStateOf(false) }
    var rewardsClaimed by remember { mutableStateOf(false) }

    val referral = remember {
        ReferralProfile(
            referralId = "REF-84920",
            referralCode = "ZORIVO-PRO-984",
            referralLink = "https://zorivo.com/ref/ZORIVO-PRO-984",
            invitedUsersCount = 14,
            activeReferralsCount = 9,
            pendingRewardsUsd = 45.0,
            completedRewardsUsd = 120.0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("profile_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ACCOUNT & SECURITY",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Identity Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ElectricCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nexis Verified Client Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "UID: USR-84920 • Jurisdictions: Global / Tier-1 Verified",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // REFERRAL & AFFILIATE REWARDS CARD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BrightGold.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = "Affiliate", tint = BrightGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REFERRAL & AFFILIATE PROGRAM", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BrightGold)
                    }
                    Box(
                        modifier = Modifier
                            .background(BrightGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("GOLD TIER (20% COMMS)", fontSize = 9.sp, fontWeight = FontWeight.Black, color = BrightGold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Earn recurring commission on every contract and trade turnover generated by your invited network.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Code and Link copy box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerminalSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR REFERRAL CODE", fontSize = 9.sp, color = TextMuted)
                            Text(referral.referralCode, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = ElectricCyan)
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(referral.referralLink))
                                copiedCodeToast = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = TerminalDarkBg),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedCodeToast) "COPIED!" else "COPY LINK", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Invited Traders", fontSize = 10.sp, color = TextMuted)
                        Text("${referral.invitedUsersCount} users", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Active Volume", fontSize = 10.sp, color = TextMuted)
                        Text("${referral.activeReferralsCount} active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Earned Rewards", fontSize = 10.sp, color = TextMuted)
                        Text(
                            "$${if (rewardsClaimed) "165.00" else "120.00"} USD",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = BrightGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { rewardsClaimed = true },
                    enabled = !rewardsClaimed,
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarketGreen, contentColor = TerminalDarkBg)
                ) {
                    Text(if (rewardsClaimed) "REWARDS CLAIMED TO WALLET" else "CLAIM PENDING COMMISSION ($45.00 USD)", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECURITY & PIN CENTER (Sections 31, 32, 34)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = "Security", tint = BrightGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SECURITY & WITHDRAWAL PROTECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(10.dp))
                SecurityRow("4-Digit Withdrawal PIN", "Configured & Active", MarketGreen)
                Spacer(modifier = Modifier.height(6.dp))
                SecurityRow("App Re-Entry Protection", "PIN + Biometric Lock", ElectricCyan)
                Spacer(modifier = Modifier.height(6.dp))
                SecurityRow("Two-Factor Authentication", "TOTP Active", MarketGreen)
                Spacer(modifier = Modifier.height(6.dp))
                SecurityRow("Active Device Sessions", "1 Device (This Phone)", TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PRODUCT TRUST & TRANSPARENCY CENTER (Section 109)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Trust", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PRODUCT TRUST & TRANSPARENCY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(10.dp))
                TrustRow("Market Data Feeds", "Twelve Data API + Binance Direct")
                Spacer(modifier = Modifier.height(6.dp))
                TrustRow("Data Integrity", "Zero Artificial or Simulated Production Prices")
                Spacer(modifier = Modifier.height(6.dp))
                TrustRow("Ledger Accounting", "Double-Entry: Total Debits == Total Credits")
                Spacer(modifier = Modifier.height(6.dp))
                TrustRow("Settlement Engine", "Deterministic & Auditable Contract Evaluation")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Risk & Exposure Parameters
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("CONFIGURED RISK LIMITS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(10.dp))
                RiskRow("Single Contract Maximum Stake", "$1,000 USD / ₦1,620,000")
                Spacer(modifier = Modifier.height(6.dp))
                RiskRow("Daily Exposure Ceiling", "$10,000 USD / ₦16,200,000")
                Spacer(modifier = Modifier.height(6.dp))
                RiskRow("Maximum Open Positions", "10 Positions")
                Spacer(modifier = Modifier.height(6.dp))
                RiskRow("Withdrawal Review Threshold", "$2,000 USD")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ADMINISTRATION & SYSTEM CONTROL PORTAL
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAdmin() },
            shape = RoundedCornerShape(12.dp),
            color = TerminalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ZorivoPrimaryBlue.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = ZorivoPrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("ADMINISTRATION PORTAL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = ZorivoTextPrimary)
                        Text("Live market spreads, system health, and ledger audit", fontSize = 10.sp, color = ZorivoTextMuted)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ZorivoBlueHighlight, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SecurityRow(label: String, status: String, statusColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
    }
}

@Composable
private fun TrustRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun RiskRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
