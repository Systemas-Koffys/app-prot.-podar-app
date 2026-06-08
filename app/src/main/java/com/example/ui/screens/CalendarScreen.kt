package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarrioAnniversary
import com.example.ui.viewmodel.PruningViewModel

@Composable
fun CalendarScreen(
    viewModel: PruningViewModel,
    requests: List<RequestEntity>
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf("05") } // Default to May or show all
    var selectedAnniversary by remember { mutableStateOf<BarrioAnniversary?>(null) }

    val months = listOf(
        "04" to "Abril",
        "05" to "Mayo",
        "06" to "Junio",
        "08" to "Agosto",
        "09" to "Septiembre",
        "10" to "Octubre",
        "11" to "Noviembre"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Banner about Municipal Anniversaries
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = ForestDeep,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Cronograma de Festividades",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestDeep
                    )
                    Text(
                        text = "La Alcaldía realiza podas preventivas antes de cada aniversario de barrio para asegurar luminarias despejadas y evitar percances en desfiles.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Horizontal Month selector
        Text(
            text = "Seleccionar Mes de Programación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            months.forEach { (code, name) ->
                val isSelected = selectedMonth == code
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(colors = listOf(ForestDeep, ForestBright))
                            } else {
                                Brush.verticalGradient(colors = listOf(Color.LightGray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = 0.2f)))
                            }
                        )
                        .clickable { selectedMonth = code }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(3),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: Anniversaries in chosen month
        val anniversariesInMonth = viewModel.anniversaries.filter { it.fecha.startsWith(selectedMonth) }

        Text(
            text = "Aniversarios del Mes (${anniversariesInMonth.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (anniversariesInMonth.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay aniversarios registrados en este mes.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                anniversariesInMonth.forEach { ann ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnniversary = ann }
                            .testTag("anniversary_card_${ann.barrio}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ForestDeep.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Cake, contentDescription = null, tint = ForestDeep, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "Barrio ${ann.barrio} (Distrito ${ann.distrito})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Fecha: ${ann.fecha} • ${ann.descripcion}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        }

        // Section: Scheduled Requests in chosen month
        val requestsInMonth = requests.filter { 
            // extract month from 'YYYY-MM-DD'
            val parts = it.fechaAgendada.split("-")
            parts.size >= 2 && parts[1] == selectedMonth 
        }

        Text(
            text = "Trabajos Coordinados de Poda (${requestsInMonth.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (requestsInMonth.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Text(
                        text = "No hay podas programadas para este mes.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("calendar_scheduled_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requestsInMonth, key = { it.id }) { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when(req.estado) {
                                                "Terminado" -> StatusUrgentGreen
                                                "En Proceso" -> ForestBright
                                                else -> StatusUrgentOrange
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${req.codigo} • ${req.especie}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lugar: Barrio ${req.barrio} | Técnico: ${req.tecnicoAsignado}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        ElevatedCard(
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = req.fechaAgendada,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for Anniversary action
    if (selectedAnniversary != null) {
        val ann = selectedAnniversary!!
        AlertDialog(
            onDismissRequest = { selectedAnniversary = null },
            title = {
                Text(
                    text = "Programación por Festividad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "¿Deseas generar una orden de trabajo preventiva para el Barrio ${ann.barrio}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta acción registrará automáticamente una solicitud de mantenimiento masivo para Ornato Público programada para el aniversario del barrio (${ann.fecha}).",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.scheduleAnniversaryPruning(ann)
                        selectedAnniversary = null
                        Toast.makeText(context, "Orden preventiva de Festividad de Barrio agregada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestDeep)
                ) {
                    Text("Generar Orden")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAnniversary = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
