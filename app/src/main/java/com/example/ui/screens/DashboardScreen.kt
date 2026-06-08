package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.PruningViewModel

@Composable
fun DashboardScreen(
    viewModel: PruningViewModel,
    requests: List<RequestEntity>,
    onNavigateToList: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Compute stats
    val totalCount = requests.size
    val pendingCount = requests.count { it.estado == "Pendiente" }
    val progressCount = requests.count { it.estado == "En Proceso" }
    val completedCount = requests.count { it.estado == "Terminado" }
    
    val lowUrgency = requests.count { it.urgencia == "Baja" }
    val midUrgency = requests.count { it.urgencia == "Media" }
    val highUrgency = requests.count { it.urgencia == "Alta" }
    val criticalUrgency = requests.count { it.urgencia == "Crítica" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header of Ornato Público Tarija
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ForestDeep, ForestBright)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Dirección de Ornato Público",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gestión de Arboricultura Urbana",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monitoreo de solicitudes de poda y derribo técnico en la ciudad de Tarija.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Dropdown/Selector Filters for Dashboard
        Text(
            text = "Filtros de Análisis Rápido",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // District selector
            var distExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { distExpanded = true },
                    modifier = Modifier.fillMaxWidth().testTag("filter_distrito_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.filterDistrito == "Todos") "Distrito: Todos" else "Distrito: ${viewModel.filterDistrito}",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = distExpanded,
                    onDismissRequest = { distExpanded = false }
                ) {
                    listOf("Todos", "D-1", "D-2", "D-6", "D-8", "D-10", "D-13").forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                viewModel.filterDistrito = dist
                                distExpanded = false
                            }
                        )
                    }
                }
            }

            // Status selector
            var statusExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { statusExpanded = true },
                    modifier = Modifier.fillMaxWidth().testTag("filter_estado_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.filterEstado == "Todos") "Estado: Todos" else "Estado: ${viewModel.filterEstado}",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    listOf("Todos", "Pendiente", "En Proceso", "Terminado").forEach { est ->
                        DropdownMenuItem(
                            text = { Text(est) },
                            onClick = {
                                viewModel.filterEstado = est
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Active filters notice
        if (viewModel.filterDistrito != "Todos" || viewModel.filterEstado != "Todos") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filtrado activo: ${if (viewModel.filterDistrito != "Todos") "Distrito " + viewModel.filterDistrito else ""} ${if (viewModel.filterEstado != "Todos") "• Estado " + viewModel.filterEstado else ""}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Limpiar",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.filterDistrito = "Todos"
                        viewModel.filterEstado = "Todos"
                    }
                )
            }
        }

        // Core visual statistics section
        Text(
            text = "Estadísticas del Ciclo de Trabajo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Pendientes",
                count = pendingCount,
                color = StatusUrgentOrange,
                icon = Icons.Default.PendingActions,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "En Proceso",
                count = progressCount,
                color = ForestBright,
                icon = Icons.Default.Autorenew,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Terminados",
                count = completedCount,
                color = StatusUrgentGreen,
                icon = Icons.Default.TaskAlt,
                modifier = Modifier.weight(1f)
            )
        }

        // Elegant custom progress row representing distribution
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Progreso Operativo Global",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // ProgressBar Layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val pendingPct = if (totalCount > 0) pendingCount.toFloat() / totalCount else 0f
                    val progressPct = if (totalCount > 0) progressCount.toFloat() / totalCount else 0f
                    val completedPct = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                    if (completedPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(completedPct.coerceAtLeast(0.01f))
                                .background(StatusUrgentGreen)
                        )
                    }
                    if (progressPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(progressPct.coerceAtLeast(0.01f))
                                .background(ForestBright)
                        )
                    }
                    if (pendingPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(pendingPct.coerceAtLeast(0.01f))
                                .background(StatusUrgentOrange)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Indicators Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(label = "Terminado ($completedCount)", color = StatusUrgentGreen)
                    LegendItem(label = "En Proceso ($progressCount)", color = ForestBright)
                    LegendItem(label = "Pendiente ($pendingCount)", color = StatusUrgentOrange)
                }
            }
        }

        // Urgencies level breakdown chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Severidad y Urgencia Técnica",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Custom elegant bars representing counts of urgencies
                val maxUrgVal = listOf(lowUrgency, midUrgency, highUrgency, criticalUrgency).maxOrNull()?.coerceAtLeast(1) ?: 1
                
                UrgencyBarChartRow(label = "Crítica (Peligro)", count = criticalUrgency, maxVal = maxUrgVal, color = StatusUrgentRed)
                UrgencyBarChartRow(label = "Alta (SETAR/Servicios)", count = highUrgency, maxVal = maxUrgVal, color = StatusUrgentOrange)
                UrgencyBarChartRow(label = "Media (Preventiva)", count = midUrgency, maxVal = maxUrgVal, color = StatusUrgentYellow)
                UrgencyBarChartRow(label = "Baja (Ornamental)", count = lowUrgency, maxVal = maxUrgVal, color = StatusUrgentGreen)
            }
        }

        // Quick shortcut to Prorrogas list
        Button(
            onClick = onNavigateToList,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("action_ver_solicitudes_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestDeep)
        ) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Cuadrícula de Solicitudes", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun UrgencyBarChartRow(
    label: String,
    count: Int,
    maxVal: Int,
    color: Color
) {
    val barWeight = count.toFloat() / maxVal
    
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (barWeight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}
