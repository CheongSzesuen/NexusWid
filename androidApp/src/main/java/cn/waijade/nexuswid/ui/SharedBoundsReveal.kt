package cn.waijade.nexuswid.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.sharedBoundsReveal(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
    boundsTransform: BoundsTransform = SharedTransitionDefaults.BoundsTransform,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    resizeMode: SharedTransitionScope.ResizeMode = scaleToBounds(
        contentScale = ContentScale.Crop
    ),
    clipShape: Shape = MaterialTheme.shapes.largeIncreased,
    renderInOverlayDuringTransition: Boolean = true,
): Modifier =
    with(sharedTransitionScope) {
        this@sharedBoundsReveal
            .sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                enter = enter,
                exit = exit,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
    }