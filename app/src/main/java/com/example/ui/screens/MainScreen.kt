package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import com.example.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.SessionViewModel
import com.example.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allAuditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    
    val selectedSession = allSessions.find { it.id == selectedSessionId }
    
    // Admin View Subsections: "DASHBOARD", "CREATE"
    var adminSubView by remember { mutableStateOf("DASHBOARD") }

    Scaffold(
        topBar = {
            Column {
                // Elegant Dark Custom Header Section
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.selectSession(null)
                                    viewModel.setRole("ADMINISTRADOR")
                                    viewModel.setFilterStatus("TODOS")
                                    ToastUtils.show(context, "🏠 Navegando para o Dashboard Principal")
                                }
                                .padding(4.dp)
                        ) {
                            // Circular frame with the brand logo
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_logo_1784588798642),
                                    contentDescription = "ValidOS Logo - Ir para Início",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Column {
                                val subLabel = if (selectedSession != null) {
                                    "VALIDOS • ${selectedSession.status}"
                                } else {
                                    "VALIDOS • OPERACIONAL"
                                }
                                val mainTitle = if (selectedSession != null) {
                                    "Sessão #${selectedSession.id}"
                                } else {
                                    "ValidOS"
                                }
                                
                                Text(
                                    text = subLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mainTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedSession == null) {
                                    Text(
                                        text = "Cada serviço. Cada prova.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            fontStyle = FontStyle.Italic
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Status Badge
                        val badgeText = if (selectedSession != null) {
                            when (selectedSession.status) {
                                "PENDENTE" -> "Aguardando"
                                "INICIADA" -> "Em Andamento"
                                "FINALIZADA" -> "Finalizada"
                                "RELATORIO_LIBERADO" -> "Liberado"
                                else -> selectedSession.status
                            }
                        } else {
                            "Ativo"
                        }
                        
                        val badgeBg = if (selectedSession != null) {
                            when (selectedSession.status) {
                                "PENDENTE" -> Color(0x1A3B82F6) // blue/10
                                "INICIADA" -> Color(0x1A10B981) // green/10
                                "FINALIZADA" -> Color(0x1A10B981) // green/10
                                else -> Color(0x1AFFFFFF)
                            }
                        } else {
                            Color(0x1A3B82F6)
                        }
                        
                        val badgeBorder = if (selectedSession != null) {
                            when (selectedSession.status) {
                                "PENDENTE" -> Color(0x4D3B82F6) // blue/30
                                "INICIADA" -> Color(0x4D10B981) // green/30
                                "FINALIZADA" -> Color(0x4D10B981)
                                else -> Color(0x33FFFFFF)
                            }
                        } else {
                            Color(0x4D3B82F6)
                        }
                        
                        val badgeTextCol = if (selectedSession != null) {
                            when (selectedSession.status) {
                                "PENDENTE" -> Color(0xFF60A5FA)
                                "INICIADA" -> Color(0xFF10B981)
                                "FINALIZADA" -> Color(0xFF10B981)
                                else -> Color.White
                            }
                        } else {
                            Color(0xFF60A5FA)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, badgeBorder),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    ),
                                    color = badgeTextCol,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            
                            // Exit App Button
                            Button(
                                onClick = { (context as? android.app.Activity)?.finish() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("exit_app_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sair do App",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sair",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Role Simulation Simulator Utility Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AMBIENTE DE SIMULAÇÃO (Selecione o perfil de teste)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val roles = listOf(
                                Triple("ADMINISTRADOR", "Admin", Icons.Default.AdminPanelSettings),
                                Triple("PRESTADOR", "Prestador", Icons.Default.Construction),
                                Triple("GERENTE", "Gerente", Icons.Default.Storefront)
                            )
                            roles.forEach { (roleKey, label, icon) ->
                                val selected = currentRole == roleKey
                                FilterChip(
                                    selected = selected,
                                    onClick = { 
                                        viewModel.setRole(roleKey)
                                    },
                                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = Color.Transparent,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("role_chip_$roleKey")
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentRole) {
                "ADMINISTRADOR" -> {
                    if (selectedSessionId == null) {
                        if (adminSubView == "CREATE") {
                            CreateSessionScreen(
                                onBack = { adminSubView = "DASHBOARD" },
                                onCreate = { company, store, address, date, time, desc, sType, pName, pCompany, pPhone, pEmail, gName, gPhone, gEmail, obs, emitOS ->
                                    viewModel.createSession(
                                        company, store, address, date, time, desc, sType,
                                        pName, pCompany, pPhone, pEmail, gName, gPhone, gEmail, obs, emitOS
                                    )
                                    adminSubView = "DASHBOARD"
                                }
                            )
                        } else {
                            AdminDashboard(
                                sessions = allSessions,
                                auditLogs = allAuditLogs,
                                onSelectSession = { id -> viewModel.selectSession(id) },
                                onCreateNewSession = { adminSubView = "CREATE" },
                                viewModel = viewModel
                            )
                        }
                    } else {
                        selectedSession?.let { session ->
                            AdminSessionDetails(
                                session = session,
                                onBack = { viewModel.selectSession(null) },
                                viewModel = viewModel
                            )
                        } ?: run {
                            viewModel.selectSession(null)
                        }
                    }
                }
                "PRESTADOR" -> {
                    if (selectedSessionId == null) {
                        PrestadorNoSessionScreen(sessions = allSessions, onSelect = { id ->
                            viewModel.selectSession(id)
                        })
                    } else {
                        selectedSession?.let { session ->
                            PrestadorFlowScreen(
                                session = session,
                                onBack = { viewModel.selectSession(null) },
                                viewModel = viewModel
                            )
                        } ?: run {
                            viewModel.selectSession(null)
                        }
                    }
                }
                "GERENTE" -> {
                    if (selectedSessionId == null) {
                        GerenteNoSessionScreen(sessions = allSessions, onSelect = { id ->
                            viewModel.selectSession(id)
                        })
                    } else {
                        selectedSession?.let { session ->
                            GerenteFlowScreen(
                                session = session,
                                onBack = { viewModel.selectSession(null) },
                                viewModel = viewModel
                            )
                        } ?: run {
                            viewModel.selectSession(null)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMINISTRADOR SCREENS
// ==========================================

@Composable
fun InitialDashboardSummaryPanel(
    totalPending: Int,
    totalDone: Int,
    totalAll: Int,
    currentFilter: String?,
    onFilterSelect: (String?) -> Unit,
    onOpenPendingModal: (() -> Unit)? = null,
    onOpenCompletedModal: (() -> Unit)? = null,
    onOpenSmartAppointmentModal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("initial_dashboard_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with icon and descriptive text
            val context = LocalContext.current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onFilterSelect("TODOS")
                        ToastUtils.show(context, "📊 Filtro: Exibindo todas as ordens de serviço")
                    }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Ir para Dashboard Resumo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "DASHBOARD INICIAL • RESUMO GERAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Toque no ícone ou card para ir direto ao caminho correspondente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // The two requested main cards (Pendentes and Finalizadas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CARD PENDENTES
                val isPendingSelected = currentFilter == "PENDENTE"
                Card(
                    onClick = { 
                        if (onOpenPendingModal != null) {
                            onOpenPendingModal()
                        } else {
                            if (isPendingSelected) {
                                onFilterSelect("TODOS")
                                ToastUtils.show(context, "📊 Exibindo TODAS as sessões")
                            } else {
                                onFilterSelect("PENDENTE")
                                ToastUtils.show(context, "⏳ Filtrando por PENDENTES")
                            }
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPendingSelected) {
                            Color(0xFFFEF3C7) // warm amber
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(
                        width = if (isPendingSelected) 2.5.dp else 1.dp,
                        color = if (isPendingSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .testTag("summary_card_pending")
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
                            Text(
                                text = "PENDENTES",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFD97706)
                            )
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Ir para Pendentes",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            text = totalPending.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFFD97706)
                        )
                        
                        Text(
                            text = "Toque para Gestão de Cobrança",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // CARD FINALIZADAS
                val isDoneSelected = currentFilter == "FINALIZADA" || currentFilter == "ENCERRADA"
                Card(
                    onClick = { 
                        if (onOpenCompletedModal != null) {
                            onOpenCompletedModal()
                        } else {
                            if (isDoneSelected) {
                                onFilterSelect("TODOS")
                                ToastUtils.show(context, "📊 Exibindo TODAS as sessões")
                            } else {
                                onFilterSelect("FINALIZADA")
                                ToastUtils.show(context, "✅ Filtrando por FINALIZADAS")
                            }
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDoneSelected) {
                            Color(0xFFD1FAE5) // warm green
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(
                        width = if (isDoneSelected) 2.5.dp else 1.dp,
                        color = if (isDoneSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .testTag("summary_card_completed")
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
                            Text(
                                text = "FINALIZADAS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF10B981)
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Ir para Finalizadas",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Text(
                            text = totalDone.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF10B981)
                        )
                        
                        Text(
                            text = "Toque para Relatório do Dia",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Total summary bar showing all sessions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                    .clickable { onFilterSelect("TODOS") }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Total Geral de Ordens de Serviço",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = totalAll.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (onOpenSmartAppointmentModal != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenSmartAppointmentModal,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_open_smart_appointment_system"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("🧠 Painel Inteligente de Agendamentos & SLA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalClearancePassCard(
    session: ServiceSession,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isReleased = session.gerenteOsNumber != null || session.gerenteOsEmitted || session.gerenteConfirmedAt != null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isReleased) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isReleased) Color(0xFF10B981) else Color(0xFFF59E0B)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isReleased) Icons.Default.VerifiedUser else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (isReleased) Color(0xFF047857) else Color(0xFFB45309),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isReleased) "CARTÃO DIGITAL DE LIBERAÇÃO" else "LIBERAÇÃO DE ACESSO PENDENTE",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = if (isReleased) Color(0xFF047857) else Color(0xFFB45309)
                    )
                }

                Surface(
                    color = if (isReleased) Color(0xFF10B981) else Color(0xFFF59E0B),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isReleased) "🟢 ACESSO AUTORIZADO" else "⏳ AGUARDANDO OS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                color = if (isReleased) Color(0xFFA7F3D0) else Color(0xFFFDE68A)
            )

            if (isReleased) {
                // Emitted OS Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ORDEM DE SERVIÇO (OS) Nº",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = session.gerenteOsNumber ?: "OS-${session.id}-LIBERADA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF047857)
                        )
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code de Liberação",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "HASH #${session.id}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RowValue(label = "Prestador Liberado", value = session.prestadorName)
                    RowValue(label = "CPF / Doc", value = session.prestadorCpf ?: "Cadastrado")
                    RowValue(label = "Empresa", value = session.prestadorCompany)
                    RowValue(label = "Loja / Local", value = "${session.storeName} (${session.company})")
                    RowValue(label = "Gerente Autenticador", value = session.gerenteName)
                    RowValue(label = "Data/Horário Válidos", value = "${session.date} às ${session.time}")
                }

                Surface(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Apresente esta tela ou o número da OS na recepção/portaria para liberação da entrada física.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // Share buttons for clearance card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val clearanceMsg = "🎟️ CARTÃO DIGITAL DE LIBERAÇÃO DE ACESSO\n\n" +
                            "✅ STATUS: ACESSO AUTORIZADO\n" +
                            "📋 ORDEM DE SERVIÇO (OS): ${session.gerenteOsNumber ?: "OS-${session.id}-LIBERADA"}\n" +
                            "👤 PRESTADOR: ${session.prestadorName} (CPF: ${session.prestadorCpf ?: "Cadastrado"})\n" +
                            "🏢 EMPRESA: ${session.prestadorCompany}\n" +
                            "📍 LOJA: ${session.storeName} (${session.company})\n" +
                            "👔 GERENTE RESPONSÁVEL: ${session.gerenteName}\n" +
                            "📅 DATA/HORA: ${session.date} às ${session.time}\n\n" +
                            "Acesse o portal via web: ${session.getPrestadorLink()}"

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(clearanceMsg))
                            ToastUtils.show(
                                context,
                                "📋 Dados do Cartão de Liberação copiados!",
                                android.widget.Toast.LENGTH_LONG
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("COPIAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }

                    Button(
                        onClick = {
                            com.example.util.PdfGenerator.printOrSavePdf(context, session)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("🖨️ PDF / PRINT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, clearanceMsg)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Enviar Cartão de Liberação"))
                        },
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF047857))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("ENVIAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = Color(0xFF047857))
                    }
                }
            } else {
                // Pending OS
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Aguardando cadastro da Ordem de Serviço (OS) de Liberação pela Gerência (${session.gerenteName}).",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Assim que a gerente validar seus documentos e gerar a OS, os dados de liberação e o QR Code de acesso serão exibidos aqui neste link web automaticamente.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

@Composable
fun LgpdDataProtectionBanner(
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Proteção LGPD",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "PROTEÇÃO DE DADOS (LGPD - LEI 13.709/2018) & REGISTRO IMUTÁVEL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Os documentos civis e dados informados são mantidos em banco de dados seguro para uso exclusivo da gerência e segurança corporativa. Conforme as diretrizes legais de conformidade, nenhum usuário possui permissão de excluir documentos ou registros salvos.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ShareableLinkCard(
    title: String,
    targetName: String,
    targetRole: String, // "PRESTADOR" or "GERENTE"
    linkUrl: String,
    shareMessage: String,
    onSimulate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = targetRole,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Destinatário: $targetName",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            // URL Display Field
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = linkUrl,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Copy Link
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(linkUrl))
                        copied = true
                        ToastUtils.show(
                            context,
                            "📋 Link do $targetRole copiado com sucesso!\nCole no WhatsApp, E-mail ou Navegador.",
                            android.widget.Toast.LENGTH_LONG
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copied) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (copied) "COPIADO!" else "COPIAR LINK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Button 2: Share via WhatsApp / System Share
                OutlinedButton(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(
                            sendIntent,
                            "Enviar Link de Liberação do $targetRole"
                        )
                        context.startActivity(shareIntent)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.2f).height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ENVIAR / SHARE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Optional Button 3: Simulate in App
                if (onSimulate != null) {
                    IconButton(
                        onClick = onSimulate,
                        modifier = Modifier.size(38.dp).background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Simular no App",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickLinkShareButtonsRow(
    session: ServiceSession,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "LINKS DE LIBERAÇÃO DE ACESSO WEB",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Navegador / Celular",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Prestador Link Button
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(session.getPrestadorLink()))
                    ToastUtils.show(
                        context,
                        "📋 Link do Prestador (${session.prestadorName}) copiado!\nCole no WhatsApp, E-mail ou Navegador.",
                        android.widget.Toast.LENGTH_LONG
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f).height(34.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Link Prestador",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Gerente Link Button
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(session.getGerenteLink()))
                    ToastUtils.show(
                        context,
                        "📋 Link da Gerente (${session.gerenteName}) copiado!\nCole no WhatsApp, E-mail ou Navegador.",
                        android.widget.Toast.LENGTH_LONG
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f).height(34.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Link Gerente",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Share both via WhatsApp / System Share Intent
            OutlinedButton(
                onClick = {
                    val msg = "🚨 LINKS DE LIBERAÇÃO DE ACESSO WEB (Sessão #${session.id} - ${session.storeName})\n\n" +
                            "👤 PRESTADOR (${session.prestadorName}):\n${session.getPrestadorLink()}\n\n" +
                            "👔 GERENTE (${session.gerenteName}):\n${session.getGerenteLink()}\n\n" +
                            "Acesse pelo navegador em qualquer dispositivo móvel ou computador."
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, msg)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Enviar Links de Liberação"))
                },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Enviar", modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun SessionWorkflowStepper(session: ServiceSession, modifier: Modifier = Modifier) {
    val isPrestadorDone = session.prestadorCpf != null || session.prestadorConfirmedAt != null
    val isGerenteDone = session.gerenteOsNumber != null || session.gerenteOsEmitted || session.gerenteConfirmedAt != null
    val isExec = session.status == "INICIADA" || session.status == "RELATORIO_LIBERADO" || session.status == "FINALIZADA" || session.status == "ENCERRADA"
    val isClosed = session.status == "FINALIZADA" || session.status == "ENCERRADA"

    val steps = listOf(
        Pair("1. Prestador", isPrestadorDone),
        Pair("2. Gerente OS", isGerenteDone),
        Pair("3. Execução", isExec),
        Pair("4. Finalizado", isClosed)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (label, done) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (done) Color(0xFF10B981) else Color(0xFFE2E8F0),
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (done) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        } else {
                            Text((idx + 1).toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFF64748B))
                        }
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = if (done) FontWeight.Bold else FontWeight.Normal),
                    color = if (done) Color(0xFF047857) else Color(0xFF64748B),
                    maxLines = 1
                )
            }
            if (idx < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.width(12.dp).padding(horizontal = 2.dp),
                    color = if (steps[idx + 1].second) Color(0xFF10B981) else Color(0xFFCBD5E1),
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
fun SessionManagementCard(
    session: ServiceSession,
    onSelectSession: (Long) -> Unit,
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copiedPrestador by remember { mutableStateOf(false) }
    var copiedGerente by remember { mutableStateOf(false) }

    val isLinkActive = session.isLinkActive()
    val isPrestadorDone = session.prestadorCpf != null || session.prestadorConfirmedAt != null
    val isGerenteDone = session.gerenteOsNumber != null || session.gerenteOsEmitted || session.gerenteConfirmedAt != null
    val isAllTasksCompleted = isPrestadorDone && (isGerenteDone || !session.emitOS)

    val cardBorderColor = when {
        !isLinkActive -> Color(0xFF94A3B8)
        isAllTasksCompleted -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_management_card_${session.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Bar: Session Header & Global Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isLinkActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLinkActive) Icons.Default.VpnKey else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isLinkActive) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SESSÃO #${session.id} • ${session.company}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLinkActive) MaterialTheme.colorScheme.primary else Color(0xFF475569)
                        )
                        Text(
                            text = "Loja: ${session.storeName} | ${session.serviceType} | ${session.date} às ${session.time}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Global Status Surface
                val (statusBg, statusText, statusIcon) = when (session.status) {
                    "PENDENTE" -> Triple(Color(0xFFFEF3C7), "⏳ PENDENTE", Icons.Default.HourglassTop)
                    "INICIADA" -> Triple(Color(0xFFDBEAFE), "🟢 EM ANDAMENTO", Icons.Default.PlayArrow)
                    "RELATORIO_LIBERADO" -> Triple(Color(0xFFF3E8FF), "📝 RELATÓRIO LIBERADO", Icons.Default.Assignment)
                    "FINALIZADA" -> Triple(Color(0xFFD1FAE5), "✅ FINALIZADA", Icons.Default.CheckCircle)
                    "ENCERRADA" -> Triple(Color(0xFFE2E8F0), "🔒 ENCERRADA / LACRADA", Icons.Default.Lock)
                    "CANCELADA" -> Triple(Color(0xFFFEE2E2), "🚫 CANCELADA", Icons.Default.Cancel)
                    else -> Triple(Color(0xFFF1F5F9), session.status, Icons.Default.Info)
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(statusIcon, contentDescription = null, modifier = Modifier.size(13.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                }
            }

            // SLA Indicator Chip
            val slaCategory = viewModel.calculateSlaCategory(session)
            val (slaBg, slaText, slaBorder) = when (slaCategory) {
                SlaCategory.PONTUAL -> Triple(Color(0xFFECFDF5), "🟢 Presença/Chegada Registrada", Color(0xFFA7F3D0))
                SlaCategory.EM_TOLERANCIA -> Triple(Color(0xFFFEF3C7), "🟡 Dentro da Tolerância (15m)", Color(0xFFFDE68A))
                SlaCategory.SLA_ESTOURADO -> Triple(Color(0xFFFEF2F2), "🔴 SLA Estourado (>15 min atrasado)", Color(0xFFFCA5A5))
                SlaCategory.AGUARDANDO -> Triple(Color(0xFFF8FAFC), "⚪ Agendado (Aguardando)", Color(0xFFE2E8F0))
                SlaCategory.CONCLUIDO -> Triple(Color(0xFFF0FDF4), "✅ Concluído / Encerrado", Color(0xFFBBF7D0))
                else -> Triple(Color(0xFFF8FAFC), "⚪ Agendado", Color(0xFFE2E8F0))
            }

            Surface(
                color = slaBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, slaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SLA de Pontualidade: $slaText",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Agendado: ${session.time}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Visual Stepper Progress Bar
            SessionWorkflowStepper(session = session)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 1. PRESTADOR SMART LINK CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !isLinkActive -> Color(0xFFF8FAFC)
                        isPrestadorDone -> Color(0xFFF0FDF4)
                        else -> Color(0xFFFFFBEB)
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    when {
                        !isLinkActive -> Color(0xFFE2E8F0)
                        isPrestadorDone -> Color(0xFFBBF7D0)
                        else -> Color(0xFFFDE68A)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = if (isLinkActive) (if (isPrestadorDone) Color(0xFF047857) else Color(0xFFB45309)) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "PRESTADOR: ${session.prestadorName}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isLinkActive) (if (isPrestadorDone) Color(0xFF047857) else Color(0xFFB45309)) else Color(0xFF475569)
                            )
                        }

                        // Status Badge for Link Prestador
                        Surface(
                            color = when {
                                !isLinkActive -> Color(0xFF64748B)
                                isPrestadorDone -> Color(0xFF10B981)
                                else -> Color(0xFFEF4444)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when {
                                    !isLinkActive -> "🔒 LINK CANCELADO"
                                    isPrestadorDone -> "🟢 DOCS ENVIADOS"
                                    else -> "⏳ DOCS PENDENTES"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // URL Box
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLinkActive) session.getPrestadorLink() else "${session.getPrestadorLink()} [🔒 LINK DESATIVADO]",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isLinkActive) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isLinkActive) {
                                    clipboardManager.setText(AnnotatedString(session.getPrestadorLink()))
                                    copiedPrestador = true
                                    ToastUtils.show(context, "📋 Link do Prestador (${session.prestadorName}) copiado!")
                                } else {
                                    ToastUtils.show(context, "🔒 LINK CANCELADO: A sessão #${session.id} foi encerrada/finalizada.", android.widget.Toast.LENGTH_LONG)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (copiedPrestador) Color(0xFF10B981) else (if (isLinkActive) MaterialTheme.colorScheme.primary else Color(0xFF64748B))
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1.2f).height(34.dp)
                        ) {
                            Icon(imageVector = if (copiedPrestador) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedPrestador) "COPIADO!" else "COPIAR LINK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }

                        OutlinedButton(
                            onClick = {
                                val statusTextMsg = if (isLinkActive) "Acesse para enviar seus documentos civis:\n${session.getPrestadorLink()}" else "SESSÃO ENCERRADA E LACRADA. Link inativo."
                                val msg = "🚨 VALIDOS • PRESTADOR (${session.prestadorName})\nSessão: ${session.serviceType} - ${session.storeName}\n$statusTextMsg"
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Enviar para Prestador"))
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ENVIAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }

                        IconButton(
                            onClick = {
                                viewModel.setRole("PRESTADOR")
                                viewModel.selectSession(session.id)
                            },
                            modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Simular Portal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 2. GERENTE SMART LINK CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        !isLinkActive -> Color(0xFFF8FAFC)
                        isGerenteDone -> Color(0xFFF0FDF4)
                        else -> Color(0xFFFEF3C7)
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    when {
                        !isLinkActive -> Color(0xFFE2E8F0)
                        isGerenteDone -> Color(0xFFBBF7D0)
                        else -> Color(0xFFFDE68A)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = if (isLinkActive) (if (isGerenteDone) Color(0xFF047857) else Color(0xFFB45309)) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "GERENTE: ${session.gerenteName}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isLinkActive) (if (isGerenteDone) Color(0xFF047857) else Color(0xFFB45309)) else Color(0xFF475569)
                            )
                        }

                        // Status Badge for Link Gerente
                        Surface(
                            color = when {
                                !isLinkActive -> Color(0xFF64748B)
                                isGerenteDone -> Color(0xFF10B981)
                                else -> Color(0xFFF59E0B)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when {
                                    !isLinkActive -> "🔒 LINK CANCELADO"
                                    isGerenteDone -> "🟢 OS EMITIDA (${session.gerenteOsNumber ?: "OK"})"
                                    else -> "⏳ OS PENDENTE"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // URL Box
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLinkActive) session.getGerenteLink() else "${session.getGerenteLink()} [🔒 LINK DESATIVADO]",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isLinkActive) MaterialTheme.colorScheme.secondary else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isLinkActive) {
                                    clipboardManager.setText(AnnotatedString(session.getGerenteLink()))
                                    copiedGerente = true
                                    ToastUtils.show(context, "📋 Link da Gerente (${session.gerenteName}) copiado!")
                                } else {
                                    ToastUtils.show(context, "🔒 LINK CANCELADO: A sessão #${session.id} foi encerrada/finalizada.", android.widget.Toast.LENGTH_LONG)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (copiedGerente) Color(0xFF10B981) else (if (isLinkActive) MaterialTheme.colorScheme.secondary else Color(0xFF64748B))
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1.2f).height(34.dp)
                        ) {
                            Icon(imageVector = if (copiedGerente) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedGerente) "COPIADO!" else "COPIAR LINK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }

                        OutlinedButton(
                            onClick = {
                                val statusTextMsg = if (isLinkActive) "Acesse para emitir a OS de Liberação:\n${session.getGerenteLink()}" else "SESSÃO ENCERRADA E LACRADA. Link inativo."
                                val msg = "🚨 VALIDOS • GERENTE (${session.gerenteName})\nSessão: ${session.serviceType} - ${session.storeName}\n$statusTextMsg"
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Enviar para Gerente"))
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ENVIAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                        }

                        IconButton(
                            onClick = {
                                viewModel.setRole("GERENTE")
                                viewModel.selectSession(session.id)
                            },
                            modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Simular Portal", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Intuitive Admin Control Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PDF Button
                Button(
                    onClick = {
                        com.example.util.PdfGenerator.printOrSavePdf(context, session)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(36.dp).weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("🖨️ PDF FICHA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                }

                // Details Button
                OutlinedButton(
                    onClick = { onSelectSession(session.id) },
                    modifier = Modifier.height(36.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("DETALHES", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }

                if (isLinkActive) {
                    // Finalize / Close Button (Invalidates links)
                    Button(
                        onClick = {
                            viewModel.closeSession(session.id)
                            ToastUtils.show(context, "🔒 Sessão #${session.id} ENCERRADA! Links web inativados por segurança.", android.widget.Toast.LENGTH_LONG)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        modifier = Modifier.height(36.dp).weight(1.1f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("🏁 ENCERRAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelSession(session.id)
                            ToastUtils.show(context, "🚫 Sessão #${session.id} CANCELADA!")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier.height(36.dp).weight(0.9f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("CANCELAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = Color(0xFFDC2626))
                    }
                } else {
                    // Reopen Button
                    Button(
                        onClick = {
                            viewModel.reopenSession(session.id)
                            ToastUtils.show(context, "🔄 Sessão #${session.id} REABERTA! Links reativados.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.height(36.dp).weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("🔄 REABRIR SESSÃO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentComplianceTrackerPanel(
    sessions: List<ServiceSession>,
    viewModel: SessionViewModel
) {
    val context = LocalContext.current
    val pendingSessions = remember(sessions) {
        sessions.filter { it.status == "PENDENTE" }
    }
    
    if (pendingSessions.isEmpty()) return
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("urgent_compliance_tracker")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "🚨 MONITOR DE TRÂMITES CRÍTICOS (SLA 24H)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Monitore obrigações prévias e cobre as partes antes do evento para evitar bloqueios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pendingSessions.forEach { session ->
                    val isPrestadorPending = session.prestadorCpf == null
                    val isGerentePending = !isPrestadorPending && session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)
                    val allOk = !isPrestadorPending && !isGerentePending
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (allOk) Color(0xFFD1FAE5).copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isPrestadorPending) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                        else if (isGerentePending) Color(0xFFF59E0B).copy(alpha = 0.4f)
                                        else Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Sessão #${session.id} - ${session.company}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isPrestadorPending) MaterialTheme.colorScheme.errorContainer
                                                        else if (isGerentePending) Color(0xFFFEF3C7)
                                                        else Color(0xFFD1FAE5),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isPrestadorPending) "Aguardando Docs" else if (isGerentePending) "Aguardando OS" else "Trâmites OK",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isPrestadorPending) MaterialTheme.colorScheme.onErrorContainer
                                                    else if (isGerentePending) Color(0xFF92400E)
                                                    else Color(0xFF065F46)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Loja: ${session.storeName} | Data: ${session.date} às ${session.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Text explanation of the pending duty
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPrestadorPending) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isPrestadorPending) MaterialTheme.colorScheme.error else if (isGerentePending) Color(0xFFD97706) else Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isPrestadorPending) {
                                            "Prestador ${session.prestadorName} precisa urgente enviar documentos civis para liberar a emissão da OS."
                                        } else if (isGerentePending) {
                                            "Documentos do prestador recebidos! Gerente ${session.gerenteName} deve cadastrar a OS de Liberação (SLA: 24h antes)."
                                        } else {
                                            "Trâmites prévios preenchidos com sucesso. Prontos para o evento."
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Action/Nudge Button
                            if (!allOk) {
                                Button(
                                    onClick = {
                                        val targetRole = if (isPrestadorPending) "PRESTADOR" else "GERENTE"
                                        val targetName = if (isPrestadorPending) session.prestadorName else session.gerenteName
                                        val targetLink = if (isPrestadorPending) session.getPrestadorLink() else session.getGerenteLink()
                                        val dutyDesc = if (isPrestadorPending) "Envio de RG/CPF e foto do documento civil para liberação de acesso" else "Cadastro da Ordem de Serviço (OS) de Liberação"
                                        
                                        viewModel.nudgeParticipant(session.id, targetRole, targetName, dutyDesc)
                                        
                                        val msg = "🚨 LEMBRETE URGENTE DE LIBERAÇÃO DE ACESSO\n\nOlá, $targetName! Por favor, acesse o link de liberação abaixo para concluir a pendência ($dutyDesc):\n\n$targetLink"
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                            type = "text/plain"
                                        }
                                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Cobrar $targetName via WhatsApp / SMS"))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPrestadorPending) MaterialTheme.colorScheme.error else Color(0xFFF59E0B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Cobrar",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COBRAR",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Quick Share buttons row directly in compliance tracker
                        QuickLinkShareButtonsRow(session = session)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboard(
    sessions: List<ServiceSession>,
    auditLogs: List<AuditLog>,
    onSelectSession: (Long) -> Unit,
    onCreateNewSession: () -> Unit,
    viewModel: SessionViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    var displayMode by remember { mutableStateOf("LINKS_MANAGEMENT") }

    var showPendingAuditModal by remember { mutableStateOf(false) }
    var showCompletedSummaryModal by remember { mutableStateOf(false) }
    var showSmartAppointmentModal by remember { mutableStateOf(false) }
    
    // Filter logic
    val filteredSessions = remember(sessions, searchQuery, filterStatus) {
        sessions.filter { session ->
            val matchesSearch = session.company.contains(searchQuery, ignoreCase = true) ||
                    session.storeName.contains(searchQuery, ignoreCase = true) ||
                    session.prestadorName.contains(searchQuery, ignoreCase = true) ||
                    session.gerenteName.contains(searchQuery, ignoreCase = true) ||
                    session.serviceType.contains(searchQuery, ignoreCase = true) ||
                    (session.prestadorCpf ?: "").contains(searchQuery) ||
                    session.id.toString() == searchQuery
                    
            val matchesStatus = when (filterStatus) {
                "PENDENTE" -> session.status == "PENDENTE"
                "INICIADA" -> session.status == "INICIADA" || session.status == "RELATORIO_LIBERADO"
                "FINALIZADA" -> session.status == "FINALIZADA"
                "ENCERRADA" -> session.status == "ENCERRADA"
                "CANCELADA" -> session.status == "CANCELADA"
                else -> true
            }
            matchesSearch && matchesStatus
        }
    }

    // Dashboard metrics
    val totalToday = sessions.size // seeded data is filtered for demo
    val totalInProg = sessions.count { it.status == "INICIADA" || it.status == "RELATORIO_LIBERADO" }
    val totalPending = sessions.count { it.status == "PENDENTE" }
    val totalDone = sessions.count { it.status == "ENCERRADA" || it.status == "FINALIZADA" }
    val totalCanceled = sessions.count { it.status == "CANCELADA" }
    
    // Faltas: if completed and compareceu is false
    val totalFaltas = sessions.count { it.compareceuPrestador == false || it.compareceuGerente == false }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Initial summary dashboard with total of pending and completed service orders
        item {
            InitialDashboardSummaryPanel(
                totalPending = totalPending,
                totalDone = totalDone,
                totalAll = totalToday,
                currentFilter = filterStatus,
                onFilterSelect = { newFilter ->
                    viewModel.setFilterStatus(newFilter)
                },
                onOpenPendingModal = { showPendingAuditModal = true },
                onOpenCompletedModal = { showCompletedSummaryModal = true },
                onOpenSmartAppointmentModal = { showSmartAppointmentModal = true }
            )
        }

        // Stats Cards Row
        item {
            Text(
                text = "Dashboard Operacional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Grid of Cards
            val context = LocalContext.current
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardCard(
                        title = "Sessões",
                        value = totalToday.toString(),
                        subtitle = "Hoje",
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.EventNote,
                        isSelected = filterStatus == "TODOS" || filterStatus == null,
                        onClick = {
                            viewModel.setFilterStatus("TODOS")
                            ToastUtils.show(context, "📊 Exibindo todas as sessões do dia")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "Ativas",
                        value = totalInProg.toString(),
                        subtitle = "Em andamento",
                        color = Color(0xFF60A5FA),
                        icon = Icons.Default.PlayArrow,
                        isSelected = filterStatus == "INICIADA",
                        onClick = {
                            viewModel.setFilterStatus("INICIADA")
                            ToastUtils.show(context, "▶️ Filtrando por Sessões Ativas em Andamento")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "Pendentes",
                        value = totalPending.toString(),
                        subtitle = "Aguardando",
                        color = Color(0xFFF59E0B),
                        icon = Icons.Default.HourglassEmpty,
                        isSelected = filterStatus == "PENDENTE",
                        onClick = {
                            showPendingAuditModal = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardCard(
                        title = "Concluídas",
                        value = totalDone.toString(),
                        subtitle = "Relatórios/PDF",
                        color = Color(0xFF10B981),
                        icon = Icons.Default.CheckCircle,
                        isSelected = filterStatus == "FINALIZADA" || filterStatus == "ENCERRADA",
                        onClick = {
                            showCompletedSummaryModal = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "Faltas",
                        value = totalFaltas.toString(),
                        subtitle = "Ausências",
                        color = Color(0xFFEF4444),
                        icon = Icons.Default.Cancel,
                        isSelected = false,
                        onClick = {
                            showPendingAuditModal = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        title = "Canceladas",
                        value = totalCanceled.toString(),
                        subtitle = "Removidas",
                        color = Color(0xFF94A3B8),
                        icon = Icons.Default.DeleteOutline,
                        isSelected = filterStatus == "CANCELADA",
                        onClick = {
                            viewModel.setFilterStatus("CANCELADA")
                            ToastUtils.show(context, "🚫 Filtrando por Sessões Canceladas")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Custom canvas visual graphics representing SLA metrics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Indicadores de Conformidade",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ComplianceMetric(
                            title = "Comparecimento",
                            percentage = 94f,
                            accentColor = Color(0xFF10B981)
                        )
                        ComplianceMetric(
                            title = "Pontualidade",
                            percentage = 88f,
                            accentColor = Color(0xFF3B82F6)
                        )
                        ComplianceMetric(
                            title = "Aprov. Documental",
                            percentage = 100f,
                            accentColor = Color(0xFF8B5CF6)
                        )
                    }
                }
            }
        }

        // Monitoring panel for pre-requisites
        item {
            UrgentComplianceTrackerPanel(sessions = sessions, viewModel = viewModel)
        }

        // Action and Search area
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Módulo de Gestão de Sessões",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Links web gerados e controle de status de tarefas das partes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onCreateNewSession,
                        modifier = Modifier.testTag("btn_create_session")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Nova")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Criar Sessão")
                    }
                }

                // Display Mode Selector Row
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = displayMode == "LINKS_MANAGEMENT",
                        onClick = { displayMode = "LINKS_MANAGEMENT" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Gestão de Links & Tarefas", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    SegmentedButton(
                        selected = displayMode == "COMPACT",
                        onClick = { displayMode = "COMPACT" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Visão Compacta", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Pesquisar por Loja, Prestador, CPF, Serviço...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Filter status Chips
                val statusFilters = listOf("TODOS", "PENDENTE", "INICIADA", "FINALIZADA", "ENCERRADA", "CANCELADA")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusFilters.forEach { status ->
                        val isSelected = filterStatus == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilterStatus(status) },
                            label = { Text(status) },
                            modifier = Modifier.testTag("filter_chip_$status")
                        )
                    }
                }
            }
        }

        // Service Sessions List
        if (filteredSessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Vazio",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhuma sessão encontrada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredSessions) { session ->
                if (displayMode == "LINKS_MANAGEMENT") {
                    SessionManagementCard(
                        session = session,
                        onSelectSession = onSelectSession,
                        viewModel = viewModel
                    )
                } else {
                    SessionCardItem(
                        session = session,
                        onClick = { onSelectSession(session.id) }
                    )
                }
            }
        }

        // Immutable Audit Logs Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Auditoria",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trilha de Auditoria Geral (Imutável)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val limitLogs = auditLogs.take(5)
                    limitLogs.forEach { log ->
                        AuditLogItem(log = log)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    
                    if (auditLogs.size > 5) {
                        Text(
                            text = "+ ${auditLogs.size - 5} registros no histórico imutável",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showPendingAuditModal) {
        PendingAuditDialog(
            sessions = sessions.filter { it.status == "PENDENTE" },
            auditLogs = auditLogs,
            viewModel = viewModel,
            onDismiss = { showPendingAuditModal = false }
        )
    }

    if (showCompletedSummaryModal) {
        CompletedSummaryDialog(
            sessions = sessions,
            viewModel = viewModel,
            onDismiss = { showCompletedSummaryModal = false }
        )
    }

    if (showSmartAppointmentModal) {
        SmartAppointmentSystemDialog(
            sessions = sessions,
            viewModel = viewModel,
            onDismiss = { showSmartAppointmentModal = false }
        )
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(110.dp)
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Color accent icon background circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.25f), shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { onClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Filtrar por $title",
                        modifier = Modifier.size(16.dp),
                        tint = color
                    )
                }
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ComplianceMetric(
    title: String,
    percentage: Float,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12f)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = percentage * 3.6f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SessionCardItem(
    session: ServiceSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSÃO #${session.id}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session.company,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                StatusBadge(status = session.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = session.storeName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Data",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session.date} às ${session.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Serviço",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = session.serviceType,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (session.status == "PENDENTE") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (session.prestadorCpf == null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else if (session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)) Color(0xFFFEF3C7) // light amber
                            else Color(0xFFD1FAE5), // light green
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (session.prestadorCpf == null) Icons.Default.Warning
                        else if (session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)) Icons.Default.HourglassEmpty
                        else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (session.prestadorCpf == null) MaterialTheme.colorScheme.error
                        else if (session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)) Color(0xFFD97706)
                        else Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (session.prestadorCpf == null) "PENDÊNCIA: Prestador precisa enviar documentos civis"
                        else if (session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)) "PENDÊNCIA: Gerente precisa gerar OS de Liberação"
                        else "TRÂMITES OK: Pronto para liberação presencial",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (session.prestadorCpf == null) MaterialTheme.colorScheme.onErrorContainer
                        else if (session.emitOS && (session.gerenteOsNumber == null || !session.gerenteOsEmitted)) Color(0xFF92400E)
                        else Color(0xFF065F46),
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prestador: ${session.prestadorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Link simulation indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Links disponíveis",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Links Ativos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            QuickLinkShareButtonsRow(session = session)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val bgColor: Color
    val textColor: Color
    val label: String
    val icon: androidx.compose.ui.graphics.vector.ImageVector

    when (status) {
        "PENDENTE" -> {
            bgColor = Color(0xFFFEF3C7) // Yellow / Amber light
            textColor = Color(0xFFD97706) // Amber dark
            label = "⏳ PENDENTE"
            icon = Icons.Default.HourglassTop
        }
        "INICIADA" -> {
            bgColor = Color(0xFFDBEAFE) // Light blue
            textColor = Color(0xFF1D4ED8) // Blue dark
            label = "🟢 EM ANDAMENTO"
            icon = Icons.Default.PlayArrow
        }
        "RELATORIO_LIBERADO" -> {
            bgColor = Color(0xFFF3E8FF) // Purple light
            textColor = Color(0xFF6B21A8) // Purple dark
            label = "📝 RELATÓRIO LIBERADO"
            icon = Icons.Default.Assignment
        }
        "FINALIZADA" -> {
            bgColor = Color(0xFFD1FAE5) // Green light
            textColor = Color(0xFF047857) // Green dark
            label = "✅ FINALIZADA"
            icon = Icons.Default.CheckCircle
        }
        "ENCERRADA" -> {
            bgColor = Color(0xFFE2E8F0) // Grey light
            textColor = Color(0xFF334155) // Slate dark
            label = "🔒 ENCERRADA / LACRADA"
            icon = Icons.Default.Lock
        }
        "CANCELADA" -> {
            bgColor = Color(0xFFFEE2E2) // Red light
            textColor = Color(0xFFB91C1C) // Red dark
            label = "🚫 CANCELADA"
            icon = Icons.Default.Cancel
        }
        else -> {
            bgColor = Color(0xFFF1F5F9)
            textColor = Color(0xFF475569)
            label = status
            icon = Icons.Default.Info
        }
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                color = textColor
            )
        }
    }
}

@Composable
fun AuditLogItem(log: AuditLog) {
    val profileColor = when (log.userProfile) {
        "ADMINISTRADOR" -> Color(0xFF3B82F6)
        "PRESTADOR" -> Color(0xFF10B981)
        "GERENTE" -> Color(0xFF8B5CF6)
        "SISTEMA" -> Color(0xFFF59E0B)
        else -> Color.Gray
    }
    
    val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
    val formattedTime = sdf.format(Date(log.timestamp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = profileColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.userProfile,
                        color = profileColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sessão #${log.sessionId}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = log.action,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "IP: ${log.ipAddress} • Disp: ${log.device}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
    }
}

// ==========================================
// SESSION CREATION SCREEN
// ==========================================

@Composable
fun CreateSessionScreen(
    onBack: () -> Unit,
    onCreate: (
        company: String, store: String, address: String, date: String, time: String,
        desc: String, sType: String, pName: String, pCompany: String, pPhone: String, pEmail: String,
        gName: String, gPhone: String, gEmail: String, obs: String, emitOS: Boolean
    ) -> Unit
) {
    // Form States
    var company by remember { mutableStateOf("Supermercados Carrefour") }
    var storeName by remember { mutableStateOf("Carrefour - Unidade Osasco") }
    var address by remember { mutableStateOf("Av. dos Autonomistas, 1542 - Osasco, SP") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("Manutenção geral elétrica e iluminação da fachada.") }
    var serviceType by remember { mutableStateOf("Elétrica") }
    
    var prestadorName by remember { mutableStateOf("Rodrigo Oliveira") }
    var prestadorCompany by remember { mutableStateOf("Oliveira Engenharia Elétrica") }
    var prestadorPhone by remember { mutableStateOf("(11) 99999-8888") }
    var prestadorEmail by remember { mutableStateOf("rodrigo@oliveiraeng.com") }
    
    var gerenteName by remember { mutableStateOf("Julio Santos") }
    var gerentePhone by remember { mutableStateOf("(11) 98888-1111") }
    var gerenteEmail by remember { mutableStateOf("julio.santos@carrefour.com") }
    var observations by remember { mutableStateOf("Trabalho noturno necessário.") }
    var emitOS by remember { mutableStateOf(true) }

    // Set today's date as default if empty
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        date = sdf.format(Date())
        time = "19:00"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                text = "Criar Nova Sessão de Serviço",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Informações Básicas da Localidade",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Empresa Contratante *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Loja / Unidade *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço Completo *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Data Agendada *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Horário Agendado *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. Detalhes do Serviço",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Tipo de Serviço *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição Detalhada do Escopo *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. Cadastro do Prestador de Serviço",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = prestadorName,
                    onValueChange = { prestadorName = it },
                    label = { Text("Nome do Prestador *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prestadorCompany,
                    onValueChange = { prestadorCompany = it },
                    label = { Text("Empresa Prestadora *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prestadorPhone,
                        onValueChange = { prestadorPhone = it },
                        label = { Text("Telefone Prestador *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prestadorEmail,
                        onValueChange = { prestadorEmail = it },
                        label = { Text("Email Prestador *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "4. Cadastro do Gerente Responsável",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = gerenteName,
                    onValueChange = { gerenteName = it },
                    label = { Text("Nome do Gerente *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gerentePhone,
                        onValueChange = { gerentePhone = it },
                        label = { Text("Telefone Gerente *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gerenteEmail,
                        onValueChange = { gerenteEmail = it },
                        label = { Text("Email Gerente *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "5. Configurações Finais",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    label = { Text("Observações Adicionais") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = emitOS,
                        onCheckedChange = { emitOS = it },
                        modifier = Modifier.testTag("checkbox_emit_os")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exigir Emissão de Ordem de Serviço (OS)?",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Button(
            onClick = {
                if (company.isNotBlank() && storeName.isNotBlank() && address.isNotBlank() &&
                    date.isNotBlank() && time.isNotBlank() && description.isNotBlank() &&
                    serviceType.isNotBlank() && prestadorName.isNotBlank() &&
                    gerenteName.isNotBlank()
                ) {
                    onCreate(
                        company, storeName, address, date, time, description, serviceType,
                        prestadorName, prestadorCompany, prestadorPhone, prestadorEmail,
                        gerenteName, gerentePhone, gerenteEmail, observations, emitOS
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_submit_session")
        ) {
            Text("SALVAR E PUBLICAR SESSÃO", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==========================================
// ADMIN DETAILS & ACTION SCREEN
// ==========================================

@Composable
fun AdminSessionDetails(
    session: ServiceSession,
    onBack: () -> Unit,
    viewModel: SessionViewModel
) {
    val clipboardManager = LocalClipboardManager.current
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val sessionLogs = remember(auditLogs, session.id) {
        auditLogs.filter { it.sessionId == session.id }
    }
    
    // Manual Attendance states
    var prestadorPresent by remember { mutableStateOf(session.compareceuPrestador ?: true) }
    var gerentePresent by remember { mutableStateOf(session.compareceuGerente ?: true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Text(
                    text = "Sessão #${session.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusBadge(status = session.status)
        }

        TramitesLiberacaoPanel(session = session, currentRole = "ADMINISTRADOR")

        // Quick Actions panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ações Administrativas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                when (session.status) {
                    "PENDENTE" -> {
                        Text(
                            text = "A sessão aguarda as confirmações do Prestador e do Gerente. Você pode forçar o início se necessário.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.forceStartSession(session.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_force_start"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Iniciar Sessão Manualmente")
                        }
                    }
                    "INICIADA" -> {
                        Text(
                            text = "A sessão foi iniciada! Registre o comparecimento físico das partes e libere o relatório final.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Comparecimento Checkboxes
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Validação de Presença no Local",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = prestadorPresent,
                                            onCheckedChange = { 
                                                prestadorPresent = it
                                                viewModel.registerAttendance(session.id, it, gerentePresent)
                                            }
                                        )
                                        Text("Prestador Presente")
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = gerentePresent,
                                            onCheckedChange = { 
                                                gerentePresent = it
                                                viewModel.registerAttendance(session.id, prestadorPresent, it)
                                            }
                                        )
                                        Text("Gerente Presente")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.releaseTechnicalReport(session.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_release_report"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LIBERAR RELATÓRIO FINAL")
                        }
                    }
                    "RELATORIO_LIBERADO" -> {
                        Text(
                            text = "O preenchimento do relatório foi liberado para o Prestador. Aguardando finalização.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    "FINALIZADA" -> {
                        Text(
                            text = "O Prestador enviou o Relatório Técnico Final. Revise os dados abaixo e faça o encerramento definitivo.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(
                            onClick = { viewModel.closeSession(session.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_close_session"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ENCERRAR SESSÃO E GERAR PDF FINAL")
                        }
                    }
                    "ENCERRADA" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SESSÃO ENCERRADA E LACRADA (IMUTÁVEL)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                    "CANCELADA" -> {
                        Text(
                            text = "Sessão cancelada administrativa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                if (session.status != "ENCERRADA" && session.status != "CANCELADA") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.cancelSession(session.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar Sessão")
                    }
                }
            }
        }

        // Access and Links Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "LINKS WEB DE LIBERAÇÃO DE ACESSO",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Copie ou envie os links abaixo por WhatsApp/E-mail. Eles funcionam em qualquer celular ou navegador.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Prestador Link Card
                ShareableLinkCard(
                    title = "Link de Acesso do Prestador",
                    targetName = session.prestadorName,
                    targetRole = "PRESTADOR",
                    linkUrl = session.getPrestadorLink(),
                    shareMessage = "Olá, ${session.prestadorName}! Segue seu link de liberação para o serviço na loja ${session.storeName} (${session.company}) agendado para ${session.date} às ${session.time}:\n\n${session.getPrestadorLink()}\n\nAcesse o link pelo celular para enviar seus documentos civis e realizar o check-in.",
                    onSimulate = {
                        viewModel.setRole("PRESTADOR")
                        viewModel.selectSession(session.id)
                    }
                )

                // Gerente Link Card
                ShareableLinkCard(
                    title = "Link de Acesso da Gerente",
                    targetName = session.gerenteName,
                    targetRole = "GERENTE",
                    linkUrl = session.getGerenteLink(),
                    shareMessage = "Olá, ${session.gerenteName}! Segue seu link de liberação e acompanhamento para o serviço de ${session.serviceType} na loja ${session.storeName} (${session.company}) em ${session.date} às ${session.time}:\n\n${session.getGerenteLink()}\n\nAcesse o link para validar a documentação do prestador, emitir a OS e autorizar o acesso.",
                    onSimulate = {
                        viewModel.setRole("GERENTE")
                        viewModel.selectSession(session.id)
                    }
                )
            }
        }

        // Sessão Ata Digital (Checklist Status)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ata Digital de Conformidade",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val items = listOf(
                    "Prestador identificado" to (session.prestadorCpf != null),
                    "Documento oficial enviado" to (session.prestadorDocumentUri != null),
                    "Documento civil aprovado" to (session.prestadorConfirmedAt != null),
                    "Gerente recebeu documentos" to (session.prestadorConfirmedAt != null),
                    "OS emitida no sistema" to (session.gerenteOsNumber != null),
                    "Prestador confirmou presença" to (session.prestadorConfirmedAt != null),
                    "Gerente confirmou presença" to (session.gerenteConfirmedAt != null),
                    "Sessão iniciada e registrada" to (session.status != "PENDENTE" && session.status != "CANCELADA")
                )
                
                items.forEach { (label, done) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (done) Color(0xFF10B981) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // PDF 1 Certificate Box (Compromisso)
        if (session.pdfCompromissoGeneratedAt != null) {
            PdfDocumentViewer(
                title = "DOCUMENTO 1 - COMPROMISSO DE EXECUÇÃO",
                hash = session.hashCompromisso ?: "HASH-1",
                timestamp = session.pdfCompromissoGeneratedAt,
                qrText = "validos://verify/pdf1/${session.id}",
                legalText = "Ambas as partes declararam sob pena civil que comparecerão no local, na data e horário acordados. O eventual não comparecimento ficará registrado para fins de auditoria e comprovação administrativa.",
                session = session,
                isFinal = false
            )
        }

        // PDF 2 Certificate Box (Final Report)
        if (session.status == "ENCERRADA" && session.pdfFinalGeneratedAt != null) {
            PdfDocumentViewer(
                title = "DOCUMENTO 2 - COMPROVAÇÃO DE EXECUÇÃO FINAL",
                hash = session.hashFinal ?: "HASH-2",
                timestamp = session.pdfFinalGeneratedAt,
                qrText = "validos://verify/pdf2/${session.id}",
                legalText = "Este documento certifica a conclusão da execução física do serviço acima relatado, com conformidade de todas as partes, armazenado de forma imutável com carimbo de tempo eletrônico no ValidOS.",
                session = session,
                isFinal = true
            )
        }

        // Technical details card if completed
        if (session.relatorioDescricao != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Relatório Técnico Consolidado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    RowValue(label = "Descrição", value = session.relatorioDescricao)
                    RowValue(label = "Concluído?", value = if (session.relatorioConcluido == true) "Sim" else "Não")
                    if (session.relatorioConcluido == false) {
                        RowValue(label = "Motivo Inconclusão", value = session.relatorioMotivo ?: "Não informado")
                    }
                    RowValue(label = "Materiais", value = session.relatorioMateriais)
                    RowValue(label = "Equipamentos", value = session.relatorioEquipamentos)
                    RowValue(label = "Peças", value = session.relatorioPecas)
                    RowValue(label = "Tempo Gasto", value = session.relatorioTempo)
                    RowValue(label = "Observações", value = session.relatorioObservacoes)
                    
                    if (session.relatorioFotoUri != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Fotos e Anexos (Enviados):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.DarkGray)
                                Text("Evidência_Técnica_01.png", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                            }
                        }
                    }

                    if (session.relatorioSignatureUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Assinatura Digital do Prestador:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ASSINADO ELETRONICAMENTE POR MARCOS SILVA",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Specific Session Audit Logs
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Logs de Auditoria desta Sessão",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                sessionLogs.forEach { log ->
                    AuditLogItem(log = log)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RowValue(label: String, value: String?) {
    value?.let {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ==========================================
// PRESTADOR FLOWS
// ==========================================

@Composable
fun PrestadorNoSessionScreen(sessions: List<ServiceSession>, onSelect: (Long) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Construction, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Portal do Prestador",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Para iniciar o fluxo do Prestador de Serviços, escolha uma das sessões agendadas abaixo ou acesse 'Simular' no painel do Administrador.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Sessões Agendadas (Prestador):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                sessions.filter { it.status == "PENDENTE" || it.status == "INICIADA" || it.status == "RELATORIO_LIBERADO" }.forEach { s ->
                    Card(
                        onClick = { onSelect(s.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${s.company} • OS #${s.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${s.storeName} | ${s.serviceType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(status = s.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrestadorFlowScreen(
    session: ServiceSession,
    onBack: () -> Unit,
    viewModel: SessionViewModel
) {
    // Form data
    val context = LocalContext.current
    var cpf by remember { mutableStateOf(session.prestadorCpf ?: "") }
    var rg by remember { mutableStateOf(session.prestadorRg ?: "") }
    var cargo by remember { mutableStateOf(session.prestadorCargo ?: "Técnico Residente") }
    var email by remember { mutableStateOf(session.prestadorEmail) }
    var documentUploaded by remember { mutableStateOf(session.prestadorDocumentUri != null) }
    
    var termsAccepted by remember { mutableStateOf(session.prestadorAcceptedTerms) }
    var lgpdAccepted by remember { mutableStateOf(session.prestadorAcceptedLgpd) }
    var horaAgendadaConfirmada by remember { mutableStateOf(false) }

    // Final Report form data
    var repDescription by remember { mutableStateOf("") }
    var repMaterials by remember { mutableStateOf("") }
    var repEquipments by remember { mutableStateOf("") }
    var repPieces by remember { mutableStateOf("") }
    var repDuration by remember { mutableStateOf("1h 30m") }
    var repObs by remember { mutableStateOf("") }
    var repIsCompleted by remember { mutableStateOf(true) }
    var repReason by remember { mutableStateOf("") }
    var repPhotoAttached by remember { mutableStateOf(false) }
    
    // Signature drawing path
    val signaturePath = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Prestador: ${session.prestadorName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (!session.isLinkActive()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                    Column {
                        Text("🔒 LINK DE ACESSO CANCELADO - PROJETO FINALIZADO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB91C1C))
                        Text("Esta sessão de serviço foi encerrada ou cancelada pelo Administrador. Os links de acesso web foram desativados por segurança LGPD. Os registros de auditoria foram mantidos e lacrados.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7F1D1D))
                    }
                }
            }
        }

        // Card de Confirmação de Chegada ao Local via Link Web
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (session.prestadorArrivedAt != null) Color(0xFFECFDF5) else Color(0xFFEFF6FF)
            ),
            border = BorderStroke(1.5.dp, if (session.prestadorArrivedAt != null) Color(0xFF10B981) else Color(0xFF3B82F6)),
            modifier = Modifier.fillMaxWidth().testTag("prestador_arrival_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (session.prestadorArrivedAt != null) Color(0xFF059669) else Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "📍 REGISTRO DE CHEGADA AO LOCAL (LINK PRESTADOR)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (session.prestadorArrivedAt != null) Color(0xFF047857) else Color(0xFF1D4ED8)
                        )
                        Text(
                            text = "Tolerância contratual de até 15 minutos do horário agendado (${session.time})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (session.prestadorArrivedAt != null) {
                    val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(session.prestadorArrivedAt!!))
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                            Column {
                                Text(
                                    text = "✅ CHEGADA REGISTRADA ÀS $formattedTime",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "📢 Notificação enviada aos 3 pertencentes do evento: ADM, Prestador (${session.prestadorName}) e Gerente (${session.gerenteName}).",
                                    color = Color(0xFF047857),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.registerArrivalViaWebLink(session.id, "PRESTADOR")
                            ToastUtils.show(context, "📍 Chegada registrada! Notificação emitida ao ADM, Prestador e Gerente!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_prestador_cheguei_local")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("📍 JÁ CHEGUEI AO LOCAL! (Confirmar Chegada)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Link Banner for Prestador Portal
        ShareableLinkCard(
            title = "Link Web de Liberação (Prestador)",
            targetName = session.prestadorName,
            targetRole = "PRESTADOR",
            linkUrl = session.getPrestadorLink(),
            shareMessage = "Olá! Segue o link web de liberação para o prestador ${session.prestadorName} na loja ${session.storeName}:\n\n${session.getPrestadorLink()}"
        )

        // Cartão Digital de Liberação (Exibição em tempo real do Acesso)
        DigitalClearancePassCard(session = session)

        // Banner de Conformidade e Proteção de Dados LGPD
        LgpdDataProtectionBanner()

        // Details of Agenda
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DADOS DO AGENDAMENTO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(session.company, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Loja: ${session.storeName}")
                Text("Data/Hora: ${session.date} às ${session.time}")
                Text("Descrição: ${session.description}", style = MaterialTheme.typography.bodySmall)
                Text("Status Geral: ${session.status}", fontWeight = FontWeight.Bold)
            }
        }

        TramitesLiberacaoPanel(session = session, currentRole = "PRESTADOR")

        when (session.status) {
            "PENDENTE" -> {
                // If not confirmed yet
                if (session.prestadorConfirmedAt == null) {
                    Text(
                        text = "Identificação Civil e Aceites de Segurança",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = cpf,
                        onValueChange = { cpf = it },
                        label = { Text("CPF *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rg,
                        onValueChange = { rg = it },
                        label = { Text("RG *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cargo,
                        onValueChange = { cargo = it },
                        label = { Text("Cargo / Função *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail corporativo *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Upload de Documento com Foto (Frente e Verso) *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { documentUploaded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (documentUploaded) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = if (documentUploaded) Icons.Default.Check else Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (documentUploaded) "Foto Frente Enviada" else "Foto Frente (RG/CNH)")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = horaAgendadaConfirmada, onCheckedChange = { horaAgendadaConfirmada = it })
                                Text("Confirmo que me encontro na data e hora marcadas para o atendimento agendado (${session.date} às ${session.time}) *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
                                Text("Aceito os Termos de Uso do Estabelecimento.", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = lgpdAccepted, onCheckedChange = { lgpdAccepted = it })
                                Text("Declaro consentimento para tratamento de meus dados em conformidade com a LGPD para fins de identificação de segurança corporativa.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    val isFormComplete = cpf.isNotBlank() && rg.isNotBlank() && cargo.isNotBlank() && email.isNotBlank() && documentUploaded
                    val isAcceptancesComplete = termsAccepted && lgpdAccepted && horaAgendadaConfirmada
                    val canConfirm = isFormComplete && isAcceptancesComplete

                    if (!canConfirm) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "⚠️ REQUISITOS PARA CHECK-IN PENDENTES:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                if (!isFormComplete) {
                                    Text(
                                        text = "• Preencha CPF, RG, Cargo e faça o upload da foto do documento abaixo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (isFormComplete && !isAcceptancesComplete) {
                                    Text(
                                        text = "• Marque a caixa de confirmação de presença no local e os consentimentos de segurança acima.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (canConfirm) {
                                viewModel.confirmPrestadorPresence(
                                    session.id, cpf, rg, cargo, email, "simulated_doc_frente.png", "simulated_doc_verso.png"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canConfirm) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("btn_prestador_confirm")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null)
                            Text("📍 ESTOU NO LOCAL NO HORÁRIO ACORDADO (Fazer Check-In)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                } else {
                    // Confirmed, show submitted documents summary card with LGPD protection
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("DOCUMENTOS CIVIS ENVIADOS COM SUCESSO", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color(0xFF047857))
                            }
                            HorizontalDivider()
                            RowValue(label = "CPF Registrado", value = session.prestadorCpf ?: cpf)
                            RowValue(label = "RG Registrado", value = session.prestadorRg ?: rg)
                            RowValue(label = "Cargo / Função", value = session.prestadorCargo ?: cargo)
                            RowValue(label = "E-mail Corporativo", value = session.prestadorEmail)

                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "🔒 Documentos civis arquivados em ambiente seguro (LGPD). Operação em modo somente leitura (Sem permissão para alterar ou apagar).",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirmed, waiting for manager block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Seu comparecimento e documentos foram registrados!",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Aguardando o Gerente da Loja emitir a OS de Liberação para autorizar a sua entrada. Acompanhe a liberação no Cartão Digital acima.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            "INICIADA" -> {
                // Show PDF 1
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sessão Iniciada com Sucesso!", fontWeight = FontWeight.Bold)
                        Text("Aguarde a liberação do relatório pelo Administrador.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
                
                PdfDocumentViewer(
                    title = "PDF 1 - COMPROMISSO DE AGENDA",
                    hash = session.hashCompromisso ?: "HASH-1",
                    timestamp = session.pdfCompromissoGeneratedAt,
                    qrText = "validos://verify/pdf1/${session.id}",
                    legalText = "Ambas as partes declararam sob pena civil que comparecerão no local, na data e horário acordados.",
                    session = session,
                    isFinal = false
                )
            }
            "RELATORIO_LIBERADO" -> {
                Text("Relatório Técnico de Execução", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = repDescription,
                    onValueChange = { repDescription = it },
                    label = { Text("Descrição dos serviços executados *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = repMaterials,
                    onValueChange = { repMaterials = it },
                    label = { Text("Materiais utilizados (Ex: Fio 4mm, conector)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repEquipments,
                    onValueChange = { repEquipments = it },
                    label = { Text("Equipamentos especiais") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repPieces,
                    onValueChange = { repPieces = it },
                    label = { Text("Peças e partes substituídas") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repDuration,
                    onValueChange = { repDuration = it },
                    label = { Text("Tempo de execução aproximado *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repObs,
                    onValueChange = { repObs = it },
                    label = { Text("Observações Técnicas / Restrições") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Concluido Radio buttons
                Column {
                    Text("O serviço foi totalmente concluído? *", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = repIsCompleted, onClick = { repIsCompleted = true })
                        Text("Sim, executado com êxito")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = !repIsCompleted, onClick = { repIsCompleted = false })
                        Text("Não, incompleto / com pendência")
                    }
                }

                if (!repIsCompleted) {
                    OutlinedTextField(
                        value = repReason,
                        onValueChange = { repReason = it },
                        label = { Text("Qual o motivo de não conclusão? *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // Photo upload simulation
                Button(
                    onClick = { repPhotoAttached = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (repPhotoAttached) Color(0xFF10B981) else MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = if (repPhotoAttached) Icons.Default.Check else Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (repPhotoAttached) "Evidência Fotográfica Anexada" else "Anexar Foto de Evidência da Conclusão *")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // FINGER DRAWING SIGNATURE CANVAS
                Text("Assinatura do Prestador (Desenhe com o dedo abaixo) *", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.White)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        signaturePath.add(change.position)
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (signaturePath.size > 1) {
                                    val path = Path()
                                    path.moveTo(signaturePath[0].x, signaturePath[0].y)
                                    for (i in 1 until signaturePath.size) {
                                        path.lineTo(signaturePath[i].x, signaturePath[i].y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.Black,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            
                            if (signaturePath.isEmpty()) {
                                Text(
                                    text = "Assine aqui",
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { signaturePath.clear() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Limpar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpar Assinatura")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (repDescription.isNotBlank() && repDuration.isNotBlank() &&
                            repPhotoAttached && (repIsCompleted || repReason.isNotBlank()) &&
                            signaturePath.isNotEmpty()
                        ) {
                            viewModel.submitTechnicalReport(
                                session.id, repDescription, repMaterials, repEquipments, repPieces,
                                repDuration, repObs, repIsCompleted, repReason,
                                "attached_evidence_foto.jpg", "finger_drawn_sig"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_finish_report")
                ) {
                    Text("FINALIZAR E ENVIAR SERVIÇO", fontWeight = FontWeight.Bold)
                }
            }
            "FINALIZADA" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Relatório Técnico Submetido!", fontWeight = FontWeight.Bold)
                        Text("Aguardando homologação e encerramento pelo Administrador.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
            "ENCERRADA" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sessão Encerrada e Homologada", fontWeight = FontWeight.Bold)
                        Text("Trilha de auditoria lacrada de forma permanente.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                PdfDocumentViewer(
                    title = "PDF FINAL - COMPROVAÇÃO DE EXECUÇÃO",
                    hash = session.hashFinal ?: "HASH-2",
                    timestamp = session.pdfFinalGeneratedAt,
                    qrText = "validos://verify/pdf2/${session.id}",
                    legalText = "Este documento certifica a conclusão da execução física do serviço relatado.",
                    session = session,
                    isFinal = true
                )
            }
            "CANCELADA" -> {
                Text("Esta sessão foi cancelada pela administração.", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}


// ==========================================
// GERENTE FLOWS
// ==========================================

@Composable
fun GerenteNoSessionScreen(sessions: List<ServiceSession>, onSelect: (Long) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Portal do Gerente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Para iniciar o fluxo do Gerente da Unidade, escolha uma das sessões agendadas abaixo ou acesse 'Simular' no painel do Administrador.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Sessões na Loja (Gerente):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                sessions.filter { it.status == "PENDENTE" || it.status == "INICIADA" || it.status == "RELATORIO_LIBERADO" }.forEach { s ->
                    Card(
                        onClick = { onSelect(s.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${s.company} • OS #${s.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${s.storeName} | ${s.serviceType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(status = s.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GerenteFlowScreen(
    session: ServiceSession,
    onBack: () -> Unit,
    viewModel: SessionViewModel
) {
    val context = LocalContext.current
    var osNumber by remember { mutableStateOf("") }
    var horaGerenteConfirmada by remember { mutableStateOf(false) }
    var dadosPrestadorRecebidos by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Gerente: ${session.gerenteName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (!session.isLinkActive()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                    Column {
                        Text("🔒 LINK DE ACESSO CANCELADO - PROJETO FINALIZADO", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB91C1C))
                        Text("Esta sessão de serviço foi encerrada ou cancelada pelo Administrador. Os links de acesso web foram desativados por segurança LGPD. Os registros de auditoria foram mantidos e lacrados.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7F1D1D))
                    }
                }
            }
        }

        // Card de Confirmação de Presença/Chegada da Gerência via Link Web
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (session.gerenteArrivedAt != null) Color(0xFFECFDF5) else Color(0xFFF0F9FF)
            ),
            border = BorderStroke(1.5.dp, if (session.gerenteArrivedAt != null) Color(0xFF10B981) else Color(0xFF0284C7)),
            modifier = Modifier.fillMaxWidth().testTag("gerente_arrival_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (session.gerenteArrivedAt != null) Color(0xFF059669) else Color(0xFF0369A1),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "📍 REGISTRO DE PRESENÇA NO LOCAL (LINK GERENTE)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (session.gerenteArrivedAt != null) Color(0xFF047857) else Color(0xFF075985)
                        )
                        Text(
                            text = "Tolerância contratual de até 15 minutos do horário agendado (${session.time})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (session.gerenteArrivedAt != null) {
                    val formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(session.gerenteArrivedAt!!))
                    Surface(
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                            Column {
                                Text(
                                    text = "✅ GERENTE PRESENTE NO LOCAL ÀS $formattedTime",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "📢 Notificação emitida para os 3 pertencentes do evento: ADM, Prestador (${session.prestadorName}) e Gerente (${session.gerenteName}).",
                                    color = Color(0xFF047857),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.registerArrivalViaWebLink(session.id, "GERENTE")
                            ToastUtils.show(context, "📍 Presença confirmada! Notificação emitida ao ADM, Prestador e Gerente!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_gerente_cheguei_local")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("📍 JÁ CHEGUEI / PRESENTE NO LOCAL! (Confirmar)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Link Banner for Gerente Portal
        ShareableLinkCard(
            title = "Link Web de Liberação (Gerente)",
            targetName = session.gerenteName,
            targetRole = "GERENTE",
            linkUrl = session.getGerenteLink(),
            shareMessage = "Olá! Segue o link web de liberação e autorização para a gerente ${session.gerenteName} na loja ${session.storeName}:\n\n${session.getGerenteLink()}"
        )

        // Cartão Digital de Liberação (Visualização da Gerência)
        DigitalClearancePassCard(session = session)

        // Banner de Conformidade e Proteção de Dados LGPD
        LgpdDataProtectionBanner()

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DADOS DA CONTRATAÇÃO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(session.company, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Unidade / Loja: ${session.storeName}")
                Text("Data/Horário Marcado: ${session.date} às ${session.time}")
                Text("Descrição do Serviço: ${session.description}", style = MaterialTheme.typography.bodySmall)
                Text("Status Geral: ${session.status}", fontWeight = FontWeight.Bold)
            }
        }

        TramitesLiberacaoPanel(session = session, currentRole = "GERENTE")

        when (session.status) {
            "PENDENTE" -> {
                if (session.gerenteConfirmedAt == null) {
                    Text("Recepção e Autorização de Entrada do Prestador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    // Prestador Civil Identity Card for Verification
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("FICHA DE IDENTIFICAÇÃO CIVIL DO PRESTADOR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                            
                            HorizontalDivider()
                            
                            if (session.prestadorCpf != null) {
                                RowValue(label = "Nome Completo", value = session.prestadorName)
                                RowValue(label = "Empresa", value = session.prestadorCompany)
                                RowValue(label = "CPF", value = session.prestadorCpf)
                                RowValue(label = "RG", value = session.prestadorRg)
                                RowValue(label = "Cargo", value = session.prestadorCargo)
                                RowValue(label = "E-mail", value = session.prestadorEmail)
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Documentos e Comprovantes Anexados pelo Prestador:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFECFDF5))
                                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("DOC_RG_CPF.png", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF047857))
                                            Text("Frente / Verso", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF065F46))
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFECFDF5))
                                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color(0xFF10B981))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("COMPROVANTE_ASO.pdf", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF047857))
                                            Text("Treinamento / ASO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF065F46))
                                        }
                                    }
                                }

                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "🔒 Dados recebidos com encriptação LGPD. Registros protegidos no banco de auditoria (Somente leitura - Sem permissão para alterar ou apagar).",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Prominent PDF Generation & Print Section for Gerente
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().testTag("gerente_pdf_print_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Print,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🖨️ EMISSÃO DE PDF / IMPRESSÃO DA FICHA DO PRESTADOR",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Text(
                                            text = "A gerente pode gerar o documento oficial em PDF com dados do prestador (CPF, RG, Cargo, Empresa), status de liberação da OS e comprovantes para impressão ou arquivo.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    com.example.util.PdfGenerator.printOrSavePdf(context, session)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.weight(1.2f).height(40.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("GERAR PDF / IMPRIMIR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    com.example.util.PdfGenerator.openOrSharePdf(context, session)
                                                },
                                                modifier = Modifier.weight(1f).height(40.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("COMPARTILHAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "O prestador Marcos Silva ainda não enviou os documentos de identificação civil no portal dele. Aguarde o preenchimento para verificar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Checkboxes for validation: Date/time and data reception
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Declarações de Conformidade e Recebimento", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = horaGerenteConfirmada, onCheckedChange = { horaGerenteConfirmada = it })
                                Text("Confirmo que me encontro na data e hora marcadas para o acompanhamento do serviço (${session.date} às ${session.time}) *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = dadosPrestadorRecebidos, 
                                    onCheckedChange = { dadosPrestadorRecebidos = it },
                                    enabled = session.prestadorCpf != null
                                )
                                Text(
                                    text = if (session.prestadorCpf != null) "Confirmo que recebi os dados de identificação civil e documentos do prestador de serviço *" else "Aguardando envio de documentos pelo prestador para recebimento *", 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (session.prestadorCpf != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (session.emitOS) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Emissão de Ordem de Serviço (OS) de Liberação", fontWeight = FontWeight.Bold)
                                }
                                Text("Esta empresa exige a vinculação da Ordem de Serviço (OS) interna de liberação para autorizar a entrada do prestador de serviço.", style = MaterialTheme.typography.bodySmall)
                                OutlinedTextField(
                                    value = osNumber,
                                    onValueChange = { osNumber = it },
                                    label = { Text("Número da OS de Liberação do Prestador *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    val canConfirm = horaGerenteConfirmada && dadosPrestadorRecebidos && (!session.emitOS || osNumber.isNotBlank())

                    if (!canConfirm) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "⚠️ REQUISITOS PENDENTES PARA AUTORIZAÇÃO:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                if (session.prestadorCpf == null) {
                                    Text(
                                        text = "• O prestador de serviço Marcos Silva precisa enviar os documentos pessoais primeiro (Passo 1 do Trâmite).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                } else if (!dadosPrestadorRecebidos) {
                                    Text(
                                        text = "• Marque a caixa confirmando que recebeu e validou os documentos civis do prestador.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (!horaGerenteConfirmada) {
                                    Text(
                                        text = "• Marque a caixa confirmando sua presença no local para acompanhar o serviço.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                if (session.emitOS && osNumber.isBlank()) {
                                    Text(
                                        text = "• Insira o número da Ordem de Serviço (OS) de Liberação interna para vincular ao check-in.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (canConfirm) {
                                viewModel.confirmGerentePresence(
                                    session.id, if (session.emitOS) osNumber else null
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canConfirm) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .testTag("btn_gerente_confirm")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null)
                            Text("📍 EMITIR OS, AUTORIZAR ENTRADA E CONFIRMAR PRESENÇA LOCAL", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Você confirmou e autorizou a entrada!", fontWeight = FontWeight.Bold)
                            Text("Aguardando o Prestador efetuar as confirmações dele para a Sessão Iniciar.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            "INICIADA", "RELATORIO_LIBERADO", "FINALIZADA" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF3B82F6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sessão em Andamento!", fontWeight = FontWeight.Bold)
                        Text("O prestador está executando as atividades.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                if (session.pdfCompromissoGeneratedAt != null) {
                    PdfDocumentViewer(
                        title = "DOCUMENTO DE COMPROMISSO EMITIDO",
                        hash = session.hashCompromisso ?: "HASH-1",
                        timestamp = session.pdfCompromissoGeneratedAt,
                        qrText = "validos://verify/pdf1/${session.id}",
                        legalText = "Este documento comprova o acordo prévio assumido pelas partes.",
                        session = session,
                        isFinal = false
                    )
                }
            }
            "ENCERRADA" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sessão de Serviço Encerrada", fontWeight = FontWeight.Bold)
                        Text("Documentação finalizada com sucesso e enviada ao seu e-mail.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
                
                PdfDocumentViewer(
                    title = "PDF FINAL DE EXECUÇÃO",
                    hash = session.hashFinal ?: "HASH-2",
                    timestamp = session.pdfFinalGeneratedAt,
                    qrText = "validos://verify/pdf2/${session.id}",
                    legalText = "Trilha imutável registrada de forma irrevogável.",
                    session = session,
                    isFinal = true
                )
            }
            "CANCELADA" -> {
                Text("Esta sessão foi cancelada administrativa.", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}


// ==========================================
// BEAUTIFUL PDF CERTIFICATE VIEWER COMPONENT
// ==========================================

@Composable
fun PdfDocumentViewer(
    title: String,
    hash: String,
    timestamp: Long?,
    qrText: String,
    legalText: String,
    session: ServiceSession,
    isFinal: Boolean
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss z", Locale.getDefault())
    val dateString = timestamp?.let { sdf.format(Date(it)) } ?: ""

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Certificate Look
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPROVANTE OFICIAL DIGITAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black, thickness = 1.dp)
            
            // Grid of details in certificate
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PdfRow(label = "SESSÃO ID", value = "#${session.id}")
                PdfRow(label = "CHAVE CRIPTOGRÁFICA (SHA-256)", value = hash)
                PdfRow(label = "DATA E HORA DE EMISSÃO", value = dateString)
                PdfRow(label = "EMPRESA CONTRATANTE", value = session.company)
                PdfRow(label = "LOJA/UNIDADE RECEPTORA", value = session.storeName)
                PdfRow(label = "PRESTADOR CONFIRMADO", value = "${session.prestadorName} (${session.prestadorCompany})")
                PdfRow(label = "GERENTE CONFIRMADO", value = session.gerenteName)
                
                if (isFinal) {
                    PdfRow(label = "SERVIÇO EXECUTADO", value = session.relatorioDescricao ?: "N/A")
                    PdfRow(label = "TEMPO GASTO", value = session.relatorioTempo ?: "N/A")
                    PdfRow(label = "MATERIAIS E PEÇAS", value = session.relatorioMateriais ?: "N/A")
                    PdfRow(label = "CONCLUÍDO COM ÊXITO", value = if (session.relatorioConcluido == true) "SIM" else "NÃO")
                    if (session.relatorioConcluido == false) {
                        PdfRow(label = "MOTIVO DE INCONCLUSÃO", value = session.relatorioMotivo ?: "N/A")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Legal clause terms
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6))
                    .border(1.dp, Color.LightGray)
                    .padding(10.dp)
            ) {
                Text(
                    text = legalText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signature Locks and Simulated QR code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR code representation
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .border(1.dp, Color.Black)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing a stylized mock QR code pattern
                        val size = this.size.width
                        val step = size / 5
                        for (r in 0..4) {
                            for (c in 0..4) {
                                if ((r + c) % 2 == 0) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * step, r * step),
                                        size = androidx.compose.ui.geometry.Size(step, step)
                                    )
                                }
                            }
                        }
                    }
                }

                // Digital signature locks
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🔒 LACRADO DIGITALMENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                    Text(
                        text = "Assinatura Eletrônica MD5 / IP vinculados",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Conformidade LGPD garantida por ValidOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PdfRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun TramitesLiberacaoPanel(
    session: ServiceSession,
    currentRole: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "REQUISITOS CRÍTICOS DE LIBERAÇÃO (MALL / SHOPPING)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Regra de Segurança Estrita: O prestador NÃO consegue acessar as dependências do shopping/estabelecimento sem a Ordem de Serviço (OS) de Liberação interna! Esta OS deve ser emitida pela Gerência de forma prioritária até 24 HORAS ANTES do evento. A emissão é bloqueada até que o prestador envie seus dados civis.",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Flow steps with indicators
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // STEP 1: Prestador Personal Docs
                val step1Done = session.prestadorCpf != null
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (step1Done) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = if (step1Done) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PASSO 1: Envio de Documentos (Prestador) - URGENTE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (step1Done) Color(0xFF047857) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (step1Done) {
                                "✓ Concluído! O prestador enviou os documentos civis e fotos para análise de segurança."
                            } else {
                                "🚨 PENDENTE! O prestador precisa fornecer seus dados civis (CPF, RG, Cargo) e foto de identificação abaixo para viabilizar a emissão da OS."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                // STEP 2: OS Liberação
                val step2Done = session.gerenteOsNumber != null || session.gerenteOsEmitted
                val step2Blocked = !step1Done
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when {
                            step2Done -> Icons.Default.CheckCircle
                            step2Blocked -> Icons.Default.Lock
                            else -> Icons.Default.LockOpen
                        },
                        contentDescription = null,
                        tint = when {
                            step2Done -> Color(0xFF10B981)
                            step2Blocked -> Color.Gray
                            else -> Color(0xFFF59E0B) // amber
                        },
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PASSO 2: Emissão da OS de Liberação (Gerente) - MÍNIMO 24H ANTES",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                step2Done -> Color(0xFF047857)
                                step2Blocked -> Color.Gray
                                else -> Color(0xFFD97706)
                            }
                        )
                        Text(
                            text = when {
                                step2Done -> "✓ Emitida! Ordem de Serviço de Liberação cadastrada: #${session.gerenteOsNumber}. O acesso do prestador foi oficialmente pré-autorizado."
                                step2Blocked -> "⛔ BLOQUEADO! A Gerência está impossibilitada de emitir a OS pois os documentos do prestador ainda não foram enviados no Passo 1."
                                else -> "⏳ AGUARDANDO EMISSÃO! Os documentos foram fornecidos. A Gerência deve cadastrar a OS de Liberação imediatamente abaixo para permitir o acesso."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                // STEP 3: Check-in / Local Presence
                val step3Done = session.prestadorConfirmedAt != null
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (step3Done) Icons.Default.CheckCircle else Icons.Default.Place,
                        contentDescription = null,
                        tint = if (step3Done) Color(0xFF10B981) else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PASSO 3: Confirmação de Presença no Local (Check-in)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (step3Done) Color(0xFF047857) else Color.Gray
                        )
                        Text(
                            text = if (step3Done) {
                                "✓ Concluído! O prestador realizou o check-in presencial no horário estabelecido."
                            } else {
                                "⏳ AGUARDANDO ATENDIMENTO! Ambas as partes devem comparecer no local e confirmar presença no horário acordado."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
