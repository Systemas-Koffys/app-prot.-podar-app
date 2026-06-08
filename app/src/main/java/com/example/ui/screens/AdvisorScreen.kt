package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.PruningViewModel

@Composable
fun AdvisorScreen(
    viewModel: PruningViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ForestDeep, ForestBright)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(
                        text = "Asistente Forestal Inteligente IA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Alimentado por Gemini. Evalúa al instante el riesgo, equipamiento y compensación ecológica para Tarija.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Form Inputs
        Text(
            text = "Parámetros del Árbol",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.aiSpecie,
                    onValueChange = { viewModel.aiSpecie = it },
                    label = { Text("Especie Forestal (ej. Jacarandá, Lapacho, Sauce, Álamo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("ai_specie_input")
                )

                OutlinedTextField(
                    value = viewModel.aiDetalles,
                    onValueChange = { viewModel.aiDetalles = it },
                    label = { Text("Detalles de Riesgo e Inclinación") },
                    placeholder = { Text("Ej: Ramas secas rozando el tendido eléctrico del alumbrado, rajadura profunda en la base...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("ai_detalles_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.aiBarrio,
                        onValueChange = { viewModel.aiBarrio = it },
                        label = { Text("Barrio") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                    OutlinedTextField(
                        value = viewModel.aiDistrito,
                        onValueChange = { viewModel.aiDistrito = it },
                        label = { Text("Distrito") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Urgencia Percibida", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Baja", "Media", "Alta", "Crítica").forEach { urg ->
                        val isSelected = viewModel.aiSelectedUrgencia == urg
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        when (urg) {
                                            "Crítica" -> StatusUrgentRed
                                            "Alta" -> StatusUrgentOrange
                                            "Media" -> StatusUrgentYellow
                                            else -> StatusUrgentGreen
                                        }
                                    } else Color.LightGray.copy(alpha = 0.3f)
                                )
                                .clickable { viewModel.aiSelectedUrgencia = urg }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = urg,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.queryAiAssessment() },
                    modifier = Modifier.fillMaxWidth().testTag("ai_query_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestDeep),
                    shape = RoundedCornerShape(10.dp),
                    enabled = viewModel.aiSpecie.isNotBlank() && viewModel.aiDetalles.isNotBlank() && !viewModel.aiLoading
                ) {
                    if (viewModel.aiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ingeniero Forestal Evaluando...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar Evaluación Técnica IA", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Response displays
        AnimatedVisibility(
            visible = viewModel.aiLoading || viewModel.aiResponse != null,
            enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { 50 }) + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resultado del Informe Técnico",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_response_box"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (viewModel.aiLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = ForestDeep)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Consultando base de especies y ordenanza municipal de Tarija...",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            viewModel.aiResponse?.let { res ->
                                Text(
                                    text = res,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
