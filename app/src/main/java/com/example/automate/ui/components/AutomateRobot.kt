package com.example.automate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.automate.R

enum class RobotDisplayMode {
    SPLASH,
    CHAT_EMPTY,
    CHAT_AVATAR
}

@Composable
fun AutomateRobot(
    mode: RobotDisplayMode,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    val defaultSize = when (mode) {
        RobotDisplayMode.SPLASH -> 240.dp
        RobotDisplayMode.CHAT_EMPTY -> 180.dp
        RobotDisplayMode.CHAT_AVATAR -> 32.dp
    }

    val finalSize = size ?: defaultSize

    Image(
        painter = painterResource(id = R.drawable.image),
        contentDescription = stringResource(R.string.cd_automate_ai_assistant),
        modifier = modifier
            .size(finalSize)
            .then(
                if (mode == RobotDisplayMode.CHAT_AVATAR) Modifier.clip(CircleShape) else Modifier
            ),
        contentScale = ContentScale.Fit
    )
}
