package com.example.automate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automate.R

@Composable
fun RobotSection(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle oval background
        Box(
            modifier = Modifier
                .size(220.dp, 280.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = GenericShape { size, _ ->
                        addOval(Rect(0f, 0f, size.width, size.height))
                    }
                )
        )

        // Robot Image Placeholder
        // Replace 'R.drawable.robot' with actual image later
        Box(
            modifier = Modifier.fillMaxSize(0.7f),
            contentAlignment = Alignment.Center
        ) {
            // Trying to load a drawable if it exists, otherwise show placeholder
            // In a real scenario, we'd use a try-catch or check
            Text("Robot Image Placeholder\n(place in res/drawable/robot.png)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(16.dp)
            )

            /*
            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = "Robot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            */
        }
    }
}
