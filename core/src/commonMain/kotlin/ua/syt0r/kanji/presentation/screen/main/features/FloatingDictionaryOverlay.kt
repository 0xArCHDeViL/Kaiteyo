package ua.syt0r.kanji.presentation.screen.main.features

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FloatingDictionaryOverlay() {
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(24f) }
    var offsetY by remember { mutableStateOf(24f) }
    
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { isExpanded = false })
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(420.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .pointerInput(Unit) { detectTapGestures { } },
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Quick Dictionary", 
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { isExpanded = false }) {
                                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        var searchQuery by remember { mutableStateOf("") }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Search, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search Japanese or English...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Search results will dynamically appear here...", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        if (!isExpanded) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(64.dp)
                    .padding(8.dp)
                    .graphicsLayer {
                        shadowElevation = 16.dp.toPx()
                        shape = CircleShape
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { isExpanded = true }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Translate, 
                    contentDescription = "Floating Dictionary",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
