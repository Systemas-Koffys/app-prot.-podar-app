package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RequestEntity
import com.example.ui.theme.ForestBright
import com.example.ui.theme.ForestDeep
import com.example.ui.theme.StatusUrgentGreen
import com.example.ui.theme.StatusUrgentOrange
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.theme.StatusUrgentYellow
import com.example.ui.viewmodel.PruningViewModel

@Composable
fun RequestsScreen(
    viewModel: PruningViewModel,
    requests: List<RequestEntity>
) {
    val context = LocalContext.current
    var isCreatingNew by remember { mutableStateOf(false) }
    var editingRequest by remember { mutableStateOf<RequestEntity?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_request_input"),
                placeholder = { Text("Buscar por código, barrio, especie o detalles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar busqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestDeep,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Header info with role indication
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Encontrados (${requests.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Show a clean visual chip of the simulated roles
                AssistChip(
                    onClick = { },
                    label = { Text("Rol: ${viewModel.userRole}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = when(viewModel.userRole) {
                                "Administrador" -> Icons.Default.AdminPanelSettings
                                "Técnico" -> Icons.Default.Construction
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Request list or empty screen
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron solicitudes.",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Pruebe limpiando los filtros o realizando otra búsqueda.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("requests_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests, key = { it.id }) { item ->
                        RequestCard(
                            request = item,
                            viewModel = viewModel,
                            onEdit = { editingRequest = item },
                            onDelete = { viewModel.deleteRequest(item) },
                            context = context
                        )
                    }
                }
            }
        }

        // Floating Action Button to create a request
        // Admins can create immediately, and Public visitors (Vecinos) can also request a service
        // Only Técnico cannot write new requests (simulating core business flow)
        if (viewModel.userRole != "Técnico") {
            FloatingActionButton(
                onClick = { isCreatingNew = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("create_request_fab"),
                containerColor = ForestBright,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Poda")
            }
        }
    }

    // Modal dialogue for Creating request
    if (isCreatingNew) {
        RequestFormDialog(
            request = null,
            context = context,
            onDismiss = { isCreatingNew = false },
            onSave = { newRequest ->
                viewModel.saveRequest(newRequest)
                isCreatingNew = false
                Toast.makeText(context, "Solicitud guardada correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal dialogue for Editing request
    if (editingRequest != null) {
        RequestFormDialog(
            request = editingRequest,
            context = context,
            onDismiss = { editingRequest = null },
            onSave = { updated ->
                viewModel.saveRequest(updated)
                editingRequest = null
                Toast.makeText(context, "Solicitud actualizada correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun RequestCard(
    request: RequestEntity,
    viewModel: PruningViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    val urgencyColor = when(request.urgencia) {
        "Crítica" -> StatusUrgentRed
        "Alta" -> StatusUrgentOrange
        "Media" -> StatusUrgentYellow
        else -> StatusUrgentGreen
    }

    val stateBg = when(request.estado) {
        "Terminado" -> StatusUrgentGreen.copy(alpha = 0.15f)
        "En Proceso" -> ForestBright.copy(alpha = 0.15f)
        else -> StatusUrgentOrange.copy(alpha = 0.15f)
    }

    val stateColor = when(request.estado) {
        "Terminado" -> StatusUrgentGreen
        "En Proceso" -> ForestBright
        else -> StatusUrgentOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_item_card_${request.codigo}")
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Row: Code, District, Barrio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(urgencyColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = request.codigo,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestDeep
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(stateBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.estado.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = stateColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Specie and Location details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🌳 ${request.especie}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${request.distrito} • Barrio ${request.barrio}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Short detail description
            Text(
                text = request.detalles,
                fontSize = 13.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Animated expansion section
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Structured properties
                    RequestDetailRow(label = "Solicitante", value = request.nombreSolicitante)
                    RequestDetailRow(label = "Teléfono", value = request.telefonoSolicitante)
                    RequestDetailRow(label = "Técnico Asignado", value = request.tecnicoAsignado)
                    RequestDetailRow(label = "Herramientas / Requisitos", value = request.requerimientos)
                    RequestDetailRow(label = "Fecha Agendada", value = request.fechaAgendada)
                    RequestDetailRow(label = "Coordenadas GPS", value = "${request.latitud}, ${request.longitud}")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick action buttons for Tree management
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick call icon
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.telefonoSolicitante}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = "Llamar", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Llamar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // WhatsApp redirect icon
                        IconButton(
                            onClick = {
                                val text = "Hola Sr(a). ${request.nombreSolicitante}. Le escribimos de la Dirección de Ornato Público de la Alcaldía de Tarija por su solicitud ${request.codigo} de poda de ${request.especie} en Barrio ${request.barrio}. ¿Podría enviarnos fotos actualizadas del árbol?"
                                val encoded = Uri.encode(text)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=591${request.telefonoSolicitante}&text=$encoded"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Route in Google Maps external Intent
                        IconButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${request.latitud},${request.longitud}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Fallback to general geo intent
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${request.latitud},${request.longitud}?q=${request.latitud},${request.longitud}"))
                                    context.startActivity(fallback)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = "Ruta", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trazar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Edit and Delete Actions based on userRole permission
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All roles can edit, but Técnico has field restrictions within the dialog
                        TextButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_request_btn_${request.codigo}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }

                        // Only Administrador role has Delete privilege
                        if (viewModel.userRole == "Administrador") {
                            TextButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.textButtonColors(contentColor = StatusUrgentRed),
                                modifier = Modifier.testTag("delete_request_btn_${request.codigo}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 24.dp), overflow = TextOverflow.Ellipsis, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestFormDialog(
    request: RequestEntity?,
    context: Context,
    onDismiss: () -> Unit,
    onSave: (RequestEntity) -> Unit
) {
    // Determine title
    val isEdit = request != null

    // Field states
    var nombreSolicitante by remember { mutableStateOf(request?.nombreSolicitante ?: "") }
    var telefonoSolicitante by remember { mutableStateOf(request?.telefonoSolicitante ?: "") }
    var distrito by remember { mutableStateOf(request?.distrito ?: "D-1") }
    var barrio by remember { mutableStateOf(request?.barrio ?: "San Roque") }
    var especie by remember { mutableStateOf(request?.especie ?: "") }
    var urgencia by remember { mutableStateOf(request?.urgencia ?: "Media") }
    var tecnicoAsignado by remember { mutableStateOf(request?.tecnicoAsignado ?: "Ing. Juan Pérez") }
    var estado by remember { mutableStateOf(request?.estado ?: "Pendiente") }
    var requerimientos by remember { mutableStateOf(request?.requerimientos ?: "Motosierra, Escalera") }
    var latitud by remember { mutableStateOf(request?.latitud?.toString() ?: "-21.53") }
    var longitud by remember { mutableStateOf(request?.longitud?.toString() ?: "-64.73") }
    var detalles by remember { mutableStateOf(request?.detalles ?: "") }
    var fechaAgendada by remember { mutableStateOf(request?.fechaAgendada ?: "2026-06-15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Editar Solicitud ${request?.codigo}" else "Registrar Nueva Solicitud",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Warning note if user is not Admin
                Text(
                    text = "Campos marcados con (*) son obligatorios.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = nombreSolicitante,
                    onValueChange = { nombreSolicitante = it },
                    label = { Text("Nombre Solicitante *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_solicitante")
                )

                OutlinedTextField(
                    value = telefonoSolicitante,
                    onValueChange = { telefonoSolicitante = it },
                    label = { Text("Teléfono Solicitante *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_telefono")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = distrito,
                        onValueChange = { distrito = it },
                        label = { Text("Distrito *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("form_distrito")
                    )
                    OutlinedTextField(
                        value = barrio,
                        onValueChange = { barrio = it },
                        label = { Text("Barrio *") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f).testTag("form_barrio")
                    )
                }

                OutlinedTextField(
                    value = especie,
                    onValueChange = { especie = it },
                    label = { Text("Especie de Árbol *") },
                    placeholder = { Text("Ej: Jacarandá, Sauce, Pino") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_especie")
                )

                // Urgencia Dropdown replacement
                Text("Urgencia técnica", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Baja", "Media", "Alta", "Crítica").forEach { urg ->
                        val isSelected = urgencia == urg
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
                                .clickable { urgencia = urg }
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

                // Estado Dropdown replacement
                Text("Estado actual", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Pendiente", "En Proceso", "Terminado").forEach { est ->
                        val isSelected = estado == est
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) ForestDeep else Color.LightGray.copy(alpha = 0.3f)
                                )
                                .clickable { estado = est }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = est,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tecnicoAsignado,
                    onValueChange = { tecnicoAsignado = it },
                    label = { Text("Técnico Asignado") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_tecnico")
                )

                OutlinedTextField(
                    value = requerimientos,
                    onValueChange = { requerimientos = it },
                    label = { Text("Equipamiento / Requisitos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_requerimientos")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latitud,
                        onValueChange = { latitud = it },
                        label = { Text("Latitud (GPS)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitud,
                        onValueChange = { longitud = it },
                        label = { Text("Longitud (GPS)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = fechaAgendada,
                    onValueChange = { fechaAgendada = it },
                    label = { Text("Fecha Programada (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_fecha")
                )

                OutlinedTextField(
                    value = detalles,
                    onValueChange = { detalles = it },
                    label = { Text("Detalles del Problema *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("form_detalles")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombreSolicitante.isBlank() || telefonoSolicitante.isBlank() || especie.isBlank() || detalles.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los datos obligatorios (*)", Toast.LENGTH_SHORT).show()
                    } else {
                        val latVal = latitud.toDoubleOrNull() ?: -21.5309
                        val lngVal = longitud.toDoubleOrNull() ?: -64.7314
                        
                        val autoGeneratedCode = request?.codigo ?: "PODA-${(200..999).random()}"
                        
                        val newRequest = RequestEntity(
                            id = request?.id ?: 0,
                            codigo = autoGeneratedCode,
                            nombreSolicitante = nombreSolicitante,
                            telefonoSolicitante = telefonoSolicitante,
                            distrito = distrito,
                            barrio = barrio,
                            especie = especie,
                            urgencia = urgencia,
                            tecnicoAsignado = tecnicoAsignado,
                            estado = estado,
                            requerimientos = requerimientos,
                            latitud = latVal,
                            longitud = lngVal,
                            detalles = detalles,
                            fechaAgendada = fechaAgendada,
                            fotos = request?.fotos ?: "placeholder_image"
                        )
                        onSave(newRequest)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestDeep)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
