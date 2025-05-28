package com.joincoded.bankapi.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiSoftGray

@Composable
fun LoginScreen() {
    val isDark = isSystemInDarkTheme()

    val logoRes = if (isDark) R.drawable.boki_logo_dark else R.drawable.boki_logo_light
    val faceIdRes = R.drawable.face_id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "Boki Logo",
            modifier = Modifier
                .size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = faceIdRes),
            contentDescription = "Face ID Icon",
            modifier = Modifier
                .size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Login with Face ID",
            color = BokiSoftGray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}