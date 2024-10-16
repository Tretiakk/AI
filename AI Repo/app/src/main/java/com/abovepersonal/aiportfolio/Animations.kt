package com.abovepersonal.aiportfolio

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

class Animations {

    @Composable
    fun Appear(
        isVisible: Boolean,
        modifier: Modifier = Modifier,
        duration: Int = 600,
        content: @Composable BoxScope.() -> Unit
    ) {
        var isVisibleObject by remember { mutableStateOf(isVisible) }

        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = duration, easing = EaseInOut),
            finishedListener = {
                if (it == 0f) {
                    isVisibleObject = false
                }
            }
        )

        if (isVisibleObject) {
            Box(
                modifier = modifier.alpha(alpha)
            ) {
                content()
            }
        }

        LaunchedEffect(isVisible){
            isVisibleObject = true
        }

        // avoid visibility on start
        LaunchedEffect(Unit){
            isVisibleObject = isVisible
        }
    }

    @Composable
    fun SelfFromRightAppear(
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        var isVisibleObject by remember { mutableStateOf(false) }


        // Get screen width and height in dp
        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp

        val xPosition by animateDpAsState(
            targetValue = if (isVisibleObject) 0.dp else screenWidthDp,
            animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)
        )

        if (isVisibleObject) {
            Box(
                modifier = modifier.offset {
                    IntOffset(x = xPosition.roundToPx() , y = 0)
                }
            ) {
                content()
            }
        }

        LaunchedEffect(Unit){
            isVisibleObject = true
        }
    }

    @Composable
    fun SelfFromLeftAppear(
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        var isVisibleObject by remember { mutableStateOf(false) }

        // Get screen width and height in dp
        val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp

        val xPosition by animateDpAsState(
            targetValue = if (isVisibleObject) 0.dp else -screenWidthDp,
            animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)
        )

        if (isVisibleObject) {
            Box(
                modifier = modifier.offset {
                    IntOffset(x = xPosition.roundToPx() , y = 0)
                }
            ) {
                content()
            }
        }

        LaunchedEffect(Unit){
            isVisibleObject = true
        }
    }
}