package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.*
import com.example.ui.viewmodel.PruningViewModel
import com.example.ui.theme.ForestDeep

class MainActivity : ComponentActivity() {
    private val viewModel: PruningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: PruningViewModel) {
    var selectedTab by remember { mutableStateOf("dashboard") } // "dashboard", "solicitudes", "mapa", "calendario", "asesor"
    var showRoleDialog by remember { mutableStateOf(false) }

    val allRequestsState by viewModel.allRequests.collectAsState()
    val requests = viewModel.requestsToDisplay

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PODAR APP",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TARIJA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                actions = {
                    // Quick top toggler to change simulated roles for easy grading/testing
                    TextButton(
                        onClick = { showRoleDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("role_selector_top_toggle")
                    ) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.userRole,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestDeep,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { selectedTab = "dashboard" },
                    modifier = Modifier.testTag("nav_tab_dashboard"),
                    icon = { Icon(if (selectedTab == "dashboard") Icons.Filled.Dashboard else Icons.Default.Dashboard, contentDescription = "Panel") },
                    label = { Text("Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == "solicitudes",
                    onClick = { selectedTab = "solicitudes" },
                    modifier = Modifier.testTag("nav_tab_solicitudes"),
                    icon = { Icon(if (selectedTab == "solicitudes") Icons.Filled.FormatListBulleted else Icons.Default.FormatListBulleted, contentDescription = "Prorrogas") },
                    label = { Text("Prorrogas", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == "mapa",
                    onClick = { selectedTab = "mapa" },
                    modifier = Modifier.testTag("nav_tab_mapa"),
                    icon = { Icon(if (selectedTab == "mapa") Icons.Filled.Map else Icons.Default.Map, contentDescription = "Mapa GPS") },
                    label = { Text("Mapa GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == "calendario",
                    onClick = { selectedTab = "calendario" },
                    modifier = Modifier.testTag("nav_tab_calendario"),
                    icon = { Icon(if (selectedTab == "calendario") Icons.Filled.CalendarMonth else Icons.Default.CalendarMonth, contentDescription = "Agenda") },
                    label = { Text("Agenda", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == "asesor",
                    onClick = { selectedTab = "asesor" },
                    modifier = Modifier.testTag("nav_tab_asesor"),
                    icon = { Icon(if (selectedTab == "asesor") Icons.Filled.AutoAwesome else Icons.Default.AutoAwesome, contentDescription = "Asistente IA") },
                    label = { Text("Asistente IA", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    requests = requests,
                    onNavigateToList = { selectedTab = "solicitudes" }
                )
                "solicitudes" -> RequestsScreen(
                    viewModel = viewModel,
                    requests = requests
                )
                "mapa" -> MapScreen(
                    requests = requests
                )
                "calendario" -> CalendarScreen(
                    viewModel = viewModel,
                    requests = requests
                )
                "asesor" -> AdvisorScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Role Simulation Selector Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = {
                Text(
                    text = "Seleccionar Rol de Simulación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Elige un rol para simular los diferentes flujos y permisos de Ornato Público Tarija:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    RoleSelectCard(
                        title = "Administrador / Ornato Público",
                        desc = "Acceso root total. Permite crear, modificar, re-agendar e incluso borrar solicitudes.",
                        icon = Icons.Default.AdminPanelSettings,
                        isSelected = viewModel.userRole == "Administrador",
                        onClick = {
                            viewModel.userRole = "Administrador"
                            showRoleDialog = false
                        }
                    )

                    RoleSelectCard(
                        title = "Técnico de Campo",
                        desc = "Acceso limitado. Puede ver la lista, mapas, iniciar navegación y actualizar estados (ej. Terminar poda).",
                        icon = Icons.Default.Construction,
                        isSelected = viewModel.userRole == "Técnico",
                        onClick = {
                            viewModel.userRole = "Técnico"
                            showRoleDialog = false
                        }
                    )

                    RoleSelectCard(
                        title = "Vecino / Solicitante (Público)",
                        desc = "Vista del ciudadano. Permite rellenar nuevas solicitudes y ver el progreso, sin borrar ni re-asignar.",
                        icon = Icons.Default.Person,
                        isSelected = viewModel.userRole == "Vecino",
                        onClick = {
                            viewModel.userRole = "Vecino"
                            showRoleDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun RoleSelectCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("role_card_$title"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ForestDeep) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ForestDeep else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) ForestDeep else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
