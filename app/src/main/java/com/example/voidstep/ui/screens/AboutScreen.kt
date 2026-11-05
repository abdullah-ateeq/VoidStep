package com.example.voidstep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voidstep.data.UserPreferences
import com.example.voidstep.ui.components.GlowingButton
import com.example.voidstep.ui.components.VoidBackground
import com.example.voidstep.ui.theme.ElectricBlue
import com.example.voidstep.ui.theme.NeonCyan
import com.example.voidstep.ui.theme.NeonPurple
import com.example.voidstep.ui.theme.VoidSurface

@Composable
fun AboutScreen(
    preferences: UserPreferences,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VoidBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidSurface.copy(alpha = 0.6f))
                            .border(1.dp, NeonPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "ABOUT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // App Title & Version Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(VoidSurface.copy(alpha = 0.7f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VOIDSTEP",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "An atmospheric minimalist arcade game of precision, momentum, and void evasion.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // How to Play Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(VoidSurface.copy(alpha = 0.6f))
                        .border(1.dp, VoidSurface, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "HOW TO PLAY",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonPurple,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Tap the left or right side of the screen (or swipe) to step toward the safe floating platform ahead.\n\n" +
                                "• The moment you land, the platform behind you dissolves into the infinite void. You can never go back.\n\n" +
                                "• Choose quickly before your current platform crumbles.\n\n" +
                                "• Consecutive swift steps build a streak multiplier for massive scores!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Credits Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(VoidSurface.copy(alpha = 0.6f))
                        .border(1.dp, VoidSurface, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "CREDITS & TECH",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricBlue,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Engineered with 100% Kotlin, Jetpack Compose, Material 3, Navigation Compose, and StateFlow.\n\n" +
                                "Completely offline with zero trackers, zero ads, and instant procedural audio synthesis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Back Button
            GlowingButton(
                text = "BACK TO HOME",
                isPrimary = false,
                onClick = onBackClick,
                hapticsEnabled = preferences.hapticsEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}
