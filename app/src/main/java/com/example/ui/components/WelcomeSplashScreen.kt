package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WelcomeSplashScreen(
    userName: String,
    onFinishSplash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircuitBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo with Glow Container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedShieldLockIcon()
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "EzWallet",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Personal Document & Credentials Vault",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (userName.isNotBlank()) "Welcome back, $userName" else "Welcome to your digital vault",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Enter Vault Button
                Button(
                    onClick = onFinishSplash,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_enter_ezwallet")
                ) {
                    Text(
                        text = "Open EzWallet Vault",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedShieldLockIcon() {
    var isShield by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            isShield = !isShield
        }
    }

    AnimatedContent(
        targetState = isShield,
        transitionSpec = {
            (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500))) togetherWith
                    (fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 0.8f, animationSpec = tween(500)))
        },
        label = "ShieldLockAnimation"
    ) { targetIsShield ->
        Icon(
            imageVector = if (targetIsShield) Icons.Default.Shield else Icons.Default.Lock,
            contentDescription = if (targetIsShield) "Shield Icon" else "Lock Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun CircuitBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "circuit_anim")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circuit_alpha"
    )

    val lineColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val path = Path()

        // Draw some circuit traces
        val stroke = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Line 1
        path.moveTo(0f, height * 0.2f)
        path.lineTo(width * 0.2f, height * 0.2f)
        path.lineTo(width * 0.3f, height * 0.1f)
        path.lineTo(width * 0.5f, height * 0.1f)
        
        // Line 2
        path.moveTo(width, height * 0.3f)
        path.lineTo(width * 0.8f, height * 0.3f)
        path.lineTo(width * 0.7f, height * 0.4f)
        path.lineTo(width * 0.4f, height * 0.4f)
        
        // Line 3
        path.moveTo(0f, height * 0.7f)
        path.lineTo(width * 0.1f, height * 0.7f)
        path.lineTo(width * 0.2f, height * 0.8f)
        path.lineTo(width * 0.6f, height * 0.8f)

        // Line 4
        path.moveTo(width, height * 0.8f)
        path.lineTo(width * 0.8f, height * 0.8f)
        path.lineTo(width * 0.7f, height * 0.7f)
        path.lineTo(width * 0.3f, height * 0.7f)

        drawPath(
            path = path,
            color = lineColor.copy(alpha = alphaAnim),
            style = stroke
        )

        // Draw nodes at the end of the lines
        val nodes = listOf(
            Offset(width * 0.5f, height * 0.1f),
            Offset(width * 0.4f, height * 0.4f),
            Offset(width * 0.6f, height * 0.8f),
            Offset(width * 0.3f, height * 0.7f)
        )

        for (node in nodes) {
            drawCircle(
                color = lineColor.copy(alpha = alphaAnim * 1.5f),
                radius = 6.dp.toPx(),
                center = node
            )
            drawCircle(
                color = bgColor,
                radius = 3.dp.toPx(),
                center = node
            )
        }
    }
}
