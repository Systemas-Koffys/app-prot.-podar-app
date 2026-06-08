package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestEntity
import com.example.ui.theme.*
import kotlin.math.absoluteValue

@Composable
fun MapScreen(
    requests: List<RequestEntity>
) {
    val context = LocalContext.current
    var selectedRequest by remember { mutableStateOf<RequestEntity?>(null) }
    
    // Zoom and pan offsets
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffsetX by remember { mutableStateOf(0f) }
    var panOffsetY by remember { mutableStateOf(0f) }

    // Tarija city center base coordinates (reference)
    val refLat = -21.5300
    val refLng = -64.7300
    // Scales to map coordinates to pixel offsets
    val latScale = -25000f // negative because latitude increases going North (up)
    val lngScale = 25000f

    Column(modifier = Modifier.fillMaxSize()) {
        // Map interactive instruction banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = ForestDeep, modifier = Modifier.size(16.dp))
                Text(
                    text = "Arrastra para explorar el mapa de Tarija. Presiona los marcadores para detalles.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForestDeep
                )
            }
        }

        // Map drawing area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE8ECE9)) // Soft background map linen
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
                .testTag("interactive_map_canvas")
        ) {
            // Draw City Elements via Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f + panOffsetX
                val centerY = canvasHeight / 2f + panOffsetY

                // 1. Draw Districts boundaries as large faint concentric circles or grids
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    radius = 350f * zoomScale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
                drawCircle(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    radius = 650f * zoomScale,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )

                // 2. Draw Guadalquivir River flowing diagonally from Top-Left to Bottom-Right
                val riverPath = Path().apply {
                    moveTo(centerX - 500f * zoomScale, centerY - 600f * zoomScale)
                    cubicTo(
                        centerX - 200f * zoomScale, centerY - 200f * zoomScale,
                        centerX + 100f * zoomScale, centerY + 200f * zoomScale,
                        centerX + 500f * zoomScale, centerY + 650f * zoomScale
                    )
                }
                drawPath(
                    path = riverPath,
                    color = Color(0xFF90CAF9), // Soft blue
                    style = Stroke(width = 24.dp.toPx() * zoomScale)
                )

                // 3. Draw Main Streets (Av. Las Américas, Av. Paz Estenssoro, Calle Sucre)
                // Av. Las Américas / Av. Paz Estenssoro
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 800f, centerY - 100f * zoomScale),
                    end = Offset(centerX + 800f, centerY + 300f * zoomScale),
                    strokeWidth = 8.dp.toPx()
                )
                // Calle General Trigo (across river bridge)
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 200f * zoomScale, centerY - 600f),
                    end = Offset(centerX + 200f * zoomScale, centerY + 600f),
                    strokeWidth = 6.dp.toPx()
                )
            }

            // Zoom controls overlays
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(2.dp)
            ) {
                IconButton(onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar Zoom", tint = ForestDeep)
                }
                IconButton(onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.5f) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Reducir Zoom", tint = ForestDeep)
                }
                IconButton(onClick = {
                    zoomScale = 1f
                    panOffsetX = 0f
                    panOffsetY = 0f
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Reajustar Origen", tint = ForestDeep)
                }
            }

            // Plot Request Markers dynamically
            requests.forEach { req ->
                // Calculate position based on relative coordinate system offset
                val dLat = req.latitud - refLat
                val dLng = req.longitud - refLng

                // Screen position calculation
                val markerX = (dLng * lngScale * zoomScale) + panOffsetX
                val markerY = (dLat * latScale * zoomScale) + panOffsetY

                val color = when (req.urgencia) {
                    "Crítica" -> StatusUrgentRed
                    "Alta" -> StatusUrgentOrange
                    "Media" -> StatusUrgentYellow
                    else -> StatusUrgentGreen
                }

                // Show marker bubble inside the safe view boundaries
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val scaleX = maxWidth.value
                    val scaleY = maxHeight.value
                    
                    // Center reference coordinates shifted
                    val posX = (scaleX / 2f) + markerX
                    val posY = (scaleY / 2f) + markerY

                    // Render marker if within visible screen bounds +- padding
                    if (posX in -50f..(scaleX + 50f) && posY in -50f..(scaleY + 50f)) {
                        Box(
                            modifier = Modifier
                                .offset(x = posX.dp - 15.dp, y = posY.dp - 35.dp)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedRequest = req }
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick legend explanation
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Severidad Poda:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                MapLegendRow("Rojo", "Crítica", StatusUrgentRed)
                MapLegendRow("Naranja", "Alta", StatusUrgentOrange)
                MapLegendRow("Amarillo", "Media", StatusUrgentYellow)
                MapLegendRow("Verde", "Baja", StatusUrgentGreen)
            }
        }

        // Bottom Details slide-up sheet simulation if a marker is clicked
        AnimatedVisibility(
            visible = selectedRequest != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedRequest?.let { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("map_request_details_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = req.codigo,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ForestDeep
                                )
                                Text(
                                    text = "Especie: ${req.especie}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            IconButton(onClick = { selectedRequest = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar detalles")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Barrio ${req.barrio} • Distrito ${req.distrito}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = req.detalles,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trigger external navigation in Google Maps
                        Button(
                            onClick = {
                                val intentUri = Uri.parse("google.navigation:q=${req.latitud},${req.longitud}")
                                val mapsIntent = Intent(Intent.ACTION_VIEW, intentUri)
                                mapsIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapsIntent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${req.latitud},${req.longitud}?q=${req.latitud},${req.longitud}"))
                                    context.startActivity(fallback)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestDeep),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Ruta en Google Maps", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapLegendRow(colorName: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}
