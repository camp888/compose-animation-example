package io.caamp888.composeanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.caamp888.composeanimation.ui.theme.ComposeAnimationTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeAnimationTheme {
                Box(Modifier.fillMaxSize()) {
                    CorrectUsageForGestureAnimation()
                }
            }
        }
    }
}

@Composable
fun CorrectUsageOfStateAnimation() {
    var moved by remember { mutableStateOf(false) }
    val pxToMove = with(LocalDensity.current) {
        100.dp.toPx().roundToInt()
    }
    val offset by animateIntOffsetAsState(
        targetValue = if (moved) {
            IntOffset(pxToMove, pxToMove)
        } else {
            IntOffset.Zero
        },
        label = "offset"
    )

    Box(
        modifier = Modifier
            .offset {
                offset
            }
            .background(Color.Blue)
            .size(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                moved = !moved
            }
    )
}

@Composable
fun WrongUsageOfStateAnimation() {
    var offsetState by remember {
        mutableStateOf(IntOffset.Zero)
    }

    val offsetAnimation by animateIntOffsetAsState(
        targetValue = offsetState,
        label = "offset",
    )

    Box(
        modifier = Modifier
            .offset {
                offsetAnimation
            }
            .background(Color.Red)
            .size(100.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        offsetState = IntOffset.Zero
                    },
                    onDragCancel = {
                        offsetState = IntOffset.Zero
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        offsetState = offsetState.copy(
                            x = offsetState.x + dragAmount.roundToInt()
                        )
                    }
                )
            }
    )
}

@Composable
fun CorrectUsageForGestureAnimation() {
    val coroutineScope = rememberCoroutineScope()

    val offsetAnimation = remember {
        Animatable(IntOffset.Zero, IntOffset.VectorConverter)
    }

    fun animateToInitialPosition() {
        coroutineScope.launch {
            offsetAnimation.animateTo(
                targetValue = IntOffset.Zero,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .offset {
                offsetAnimation.value
            }
            .background(Color.Green)
            .size(100.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        animateToInitialPosition()
                    },
                    onDragCancel = {
                        animateToInitialPosition()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        coroutineScope.launch {
                            offsetAnimation.snapTo(
                                targetValue = offsetAnimation.value.copy(
                                    offsetAnimation.value.x + dragAmount.roundToInt()
                                ),
                            )
                        }
                    }
                )
            }
    )
}