package com.example.automate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R
import com.example.automate.ui.components.PrimaryButton
import com.example.automate.ui.theme.AutomateTheme

private data class GuideStep(
    val number: Int,
    val titleRes: Int,
    val descriptionRes: Int
)

private val guideSteps = listOf(
    GuideStep(1, R.string.guide_step1_title, R.string.guide_step1_desc),
    GuideStep(2, R.string.guide_step2_title, R.string.guide_step2_desc),
    GuideStep(3, R.string.guide_step3_title, R.string.guide_step3_desc),
    GuideStep(4, R.string.guide_step4_title, R.string.guide_step4_desc),
    GuideStep(5, R.string.guide_step5_title, R.string.guide_step5_desc),
    GuideStep(6, R.string.guide_step6_title, R.string.guide_step6_desc)
)

@Composable
fun AppGuideScreen(
    isOnboarding: Boolean,
    onBackClick: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000C1F))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        if (!isOnboarding) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(if (isOnboarding) R.string.guide_onboarding_title else R.string.guide_help_title),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(if (isOnboarding) R.string.guide_onboarding_subtitle else R.string.guide_help_subtitle),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column {
            guideSteps.forEachIndexed { index, step ->
                GuideStepRow(step = step, isLast = index == guideSteps.lastIndex)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = stringResource(if (isOnboarding) R.string.action_get_started else R.string.action_got_it),
            onClick = onDone
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GuideStepRow(step: GuideStep, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF007BFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.number.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 24.dp)
        ) {
            Text(
                text = stringResource(R.string.guide_step_label, step.number),
                color = Color(0xFF4FA8FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(step.titleRes),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(step.descriptionRes),
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, name = "Onboarding")
@Composable
private fun AppGuideOnboardingPreview() {
    AutomateTheme {
        AppGuideScreen(isOnboarding = true, onBackClick = {}, onDone = {})
    }
}

@Preview(showBackground = true, name = "Help")
@Composable
private fun AppGuideHelpPreview() {
    AutomateTheme {
        AppGuideScreen(isOnboarding = false, onBackClick = {}, onDone = {})
    }
}
