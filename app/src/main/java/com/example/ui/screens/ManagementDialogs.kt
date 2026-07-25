package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.SessionViewModel
import com.example.util.PdfGenerator
import com.example.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingAuditDialog(
    sessions: List<ServiceSession>,
    auditLogs: List<AuditLog>,
    viewModel: SessionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSessionForNote by remember { mutableStateOf<ServiceSession?>(null) }
    var customNoteText by remember { mutableStateOf("") }
    var showNoteModal by remember { mutableStateOf(false) }
    var cobrarTarget by remember { mutableStateOf<CobrarTarget?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Gestão de Pendências & Auditoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sessions.size} sessão(ões) aguardando solução do ADM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Este painel registra todas as cobranças e tentativas de contato com Prestadores e Gerentes para auditoria comprobatória.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma pendência encontrada no momento!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Todas as ordens de serviço estão em andamento ou finalizadas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val sessionLogs = auditLogs.filter { it.sessionId == session.id }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Session Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "OS #${session.id} • ${session.company}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Loja: ${session.storeName} | ${session.serviceType}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = session.status)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Prestador Section
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                                Text("Prestador: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                Text(session.prestadorName, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(
                                                "Tel: ${session.prestadorPhone} (${session.prestadorCompany})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Call / WhatsApp Button
                                            OutlinedButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${session.prestadorPhone}"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        ToastUtils.show(context, "Telefone do prestador: ${session.prestadorPhone}")
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ligar", style = MaterialTheme.typography.labelSmall)
                                                                               // Cobrar Prestador Button
                                            Button(
                                                onClick = {
                                                    cobrarTarget = CobrarTarget(session, "PRESTADOR", session.prestadorName, session.prestadorPhone)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Cobrar", style = MaterialTheme.typography.labelSmall)
                                            }                   }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Gerente Section
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(imageVector = Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                                Text("Gerente: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                Text(session.gerenteName, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(
                                                "Tel: ${session.gerentePhone}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${session.gerentePhone}"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        ToastUtils.show(context, "Telefone do gerente: ${session.gerentePhone}")
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ligar", style = MaterialTheme.typography.labelSmall)
                                            }

                                            Button(
                                                onClick = {
                                                    cobrarTarget = CobrarTarget(session, "GERENTE", session.gerenteName, session.gerentePhone)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Cobrar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Audit History box for this session
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "📜 Histórico de Cobrança / Auditoria (${sessionLogs.size}):",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                TextButton(
                                                    onClick = {
                                                        selectedSessionForNote = session
                                                        customNoteText = ""
                                                        showNoteModal = true
                                                    },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Nova Tentativa", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }

                                            if (sessionLogs.isEmpty()) {
                                                Text(
                                                    "Sem cobranças registradas ainda. Clique em 'Cobrar' ou 'Nova Tentativa'.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            } else {
                                                sessionLogs.takeLast(3).forEach { log ->
                                                    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                                    Text(
                                                        "• [$dateStr] ${log.action}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Painel Principal")
                }
            }
        }
    }

    val currentCobrarTarget = cobrarTarget
    if (currentCobrarTarget != null) {
        CobrarDispatchModal(
            target = currentCobrarTarget,
            viewModel = viewModel,
            onDismiss = { cobrarTarget = null }
        )
    }

    // Modal to add custom contact attempt
    val targetSession = selectedSessionForNote
    if (showNoteModal && targetSession != null) {
        AlertDialog(
            onDismissRequest = { showNoteModal = false },
            title = {
                Text("Registrar Tentativa de Solução", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OS #${targetSession.id} - ${targetSession.company}", style = MaterialTheme.typography.bodySmall)

                    Text("Sugestões Rápidas de Ação:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SuggestionChip(
                            onClick = { customNoteText = "Ligado para prestador, prometeu envio em 15 min" },
                            label = { Text("Ligação OK", fontSize = 10.sp) }
                        )
                        SuggestionChip(
                            onClick = { customNoteText = "Mensagem WhatsApp enviada sem resposta" },
                            label = { Text("WhatsApp", fontSize = 10.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = customNoteText,
                        onValueChange = { customNoteText = it },
                        label = { Text("Descrição da Tentativa de Solução") },
                        placeholder = { Text("Ex: Gerente avisado sobre atraso do prestador") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customNoteText.isNotBlank()) {
                            viewModel.logCustomAction(targetSession.id, customNoteText.trim())
                            ToastUtils.show(context, "✅ Tentativa de solução registrada na auditoria!")
                            showNoteModal = false
                        }
                    }
                ) {
                    Text("Salvar Registro")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CompletedSummaryDialog(
    sessions: List<ServiceSession>,
    viewModel: SessionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val completedSessions = remember(sessions) {
        sessions.filter { it.status == "FINALIZADA" || it.status == "ENCERRADA" || it.status == "RELATORIO_LIBERADO" }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Sessões Concluídas & Relatórios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${completedSessions.size} ordem(ns) de serviço finalizadas hoje",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar for General Daily Report
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Relatório Geral do Dia",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "Gere um PDF consolidado com o resumo de todas as ordens de serviço finalizadas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (completedSessions.isNotEmpty()) {
                                    PdfGenerator.openOrShareDailySummaryPdf(context, completedSessions)
                                } else {
                                    ToastUtils.show(context, "Nenhuma sessão finalizada para gerar o relatório geral.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerar Relatório Geral do Dia (PDF)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Lista de Ordens de Serviço (Relatório Individual):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (completedSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma ordem de serviço concluída hoje ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(completedSessions, key = { it.id }) { session ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "OS #${session.id} • ${session.company}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${session.storeName} | ${session.serviceType}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = session.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Prestador: ${session.prestadorName} | Gerente: ${session.gerenteName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!session.hashFinal.isNullOrBlank()) {
                                        Text(
                                            text = "Hash Auditoria: ${session.hashFinal.take(16)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Individual PDF Report
                                        Button(
                                            onClick = {
                                                PdfGenerator.openOrSharePdf(context, session)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PDF Individual", style = MaterialTheme.typography.labelSmall)
                                        }

                                        // Print Ficha Card
                                        OutlinedButton(
                                            onClick = {
                                                PdfGenerator.printOrSavePdf(context, session)
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Imprimir Cartão", style = MaterialTheme.typography.labelSmall)
                                        }

                                        // Inspect
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.selectSession(session.id)
                                                onDismiss()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Painel")
                }
            }
        }
    }
}

data class CobrarTarget(
    val session: ServiceSession,
    val role: String,
    val name: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobrarDispatchModal(
    target: CobrarTarget,
    viewModel: SessionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customMessage by remember(target) {
        mutableStateOf(
            if (target.role == "PRESTADOR") {
                "Atenção, ${target.name}! O horário acordado (${target.session.time}) para a OS #${target.session.id} (${target.session.company} - ${target.session.storeName}) não foi cumprido. A ordem de serviço continua aberta aguardando sua chegada para ser realizada. Favor responder ou comparecer com urgência."
            } else {
                "Atenção, Gerente ${target.name}! O horário acordado (${target.session.time}) para a OS #${target.session.id} (${target.session.company}) não foi cumprido. A liberação do atendimento ainda é necessária para concluir o serviço. Favor confirmar."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Cobrar Pendência - OS #${target.session.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${target.role}: ${target.name} (${target.phone})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Delay Alert Box with 15-Minute Tolerance
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Descumprimento do Horário (Tolerância de 15 min)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        
                        Text(
                            text = "⏱️ Tolerância Regulamentar: Considerada tolerância de até 15 minutos em relação ao horário acordado (${target.session.time}).",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Será registrado no histórico de auditoria da OS #${target.session.id} que o horário não foi cumprido por ${target.name}. A OS CONTINUA ABERTA e o atendimento/evento poderá ocorrer normalmente assim que a parte pendente acessar o link web e registrar sua chegada.",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Text(
                    text = "Mensagem de cobrança personalizada:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // WhatsApp Button
                Button(
                    onClick = {
                        viewModel.nudgeParticipant(target.session.id, target.role, target.name, customMessage, "WHATSAPP")
                        val cleanPhone = target.phone.replace(Regex("[^0-9]"), "")
                        val fullPhone = if (cleanPhone.length <= 11 && cleanPhone.isNotEmpty()) "55$cleanPhone" else cleanPhone
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(customMessage)}"))
                            context.startActivity(intent)
                            ToastUtils.show(context, "📱 WhatsApp aberto para ${target.name}!")
                        } catch (e: Exception) {
                            ToastUtils.show(context, "Telefone: $fullPhone - Mensagem copiada")
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar via WhatsApp")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // SMS Button
                    OutlinedButton(
                        onClick = {
                            viewModel.nudgeParticipant(target.session.id, target.role, target.name, customMessage, "SMS")
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${target.phone}")).apply {
                                    putExtra("sms_body", customMessage)
                                }
                                context.startActivity(intent)
                                ToastUtils.show(context, "💬 SMS preparado para ${target.name}!")
                            } catch (e: Exception) {
                                ToastUtils.show(context, "Erro ao abrir SMS para ${target.phone}")
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enviar SMS", style = MaterialTheme.typography.labelSmall)
                    }

                    // Audit Only Button
                    OutlinedButton(
                        onClick = {
                            viewModel.nudgeParticipant(target.session.id, target.role, target.name, customMessage, "REGISTRO MANUAL")
                            ToastUtils.show(context, "⚠️ Cobrança e atraso registrados na auditoria!")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apenas Auditoria", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAppointmentSystemDialog(
    sessions: List<ServiceSession>,
    viewModel: SessionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val stats = viewModel.getSmartAppointmentStats(sessions)
    
    // Find all conflicts across sessions
    val allConflicts = remember(sessions) {
        val list = mutableListOf<com.example.data.SmartConflict>()
        sessions.forEach { s ->
            val c = viewModel.detectScheduleConflicts(s, sessions)
            c.forEach { item ->
                if (!list.any { existing -> existing.targetSession.id == item.conflictingSession.id && existing.conflictingSession.id == item.targetSession.id }) {
                    list.add(item)
                }
            }
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4F46E5).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "🧠 Painel Inteligente de Agendamentos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Garantia de SLA, Tolerância 15m & Anti-Conflito de Escala",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pontualidade Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pontualidade SLA", fontSize = 10.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                            Text("${stats.punctualityRatePercent}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF047857))
                            Text("meta > 95%", fontSize = 9.sp, color = Color(0xFF059669))
                        }
                    }

                    // SLA Estourado Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (stats.slaViolationsCount > 0) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, if (stats.slaViolationsCount > 0) Color(0xFFFCA5A5) else Color(0xFFBBF7D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SLA Estourado (>15m)", fontSize = 10.sp, color = if (stats.slaViolationsCount > 0) Color(0xFF991B1B) else Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text("${stats.slaViolationsCount}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (stats.slaViolationsCount > 0) Color(0xFFDC2626) else Color(0xFF15803D))
                            Text("atrasos críticos", fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Conflitos Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (allConflicts.isNotEmpty()) Color(0xFFFFFBEB) else Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, if (allConflicts.isNotEmpty()) Color(0xFFFDE68A) else Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Conflitos de Escala", fontSize = 10.sp, color = if (allConflicts.isNotEmpty()) Color(0xFF92400E) else Color(0xFF475569), fontWeight = FontWeight.Bold)
                            Text("${allConflicts.size}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (allConflicts.isNotEmpty()) Color(0xFFD97706) else Color(0xFF64748B))
                            Text("sobreposições", fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section 1: Schedule Conflicts
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Text("⚠️ Detecção Automática de Conflitos e Duplicidades", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                }

                                if (allConflicts.isEmpty()) {
                                    Surface(
                                        color = Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "✅ Nenhum conflito de horário ou sobreposição de prestadores detectado no momento.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF047857),
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                } else {
                                    allConflicts.forEach { conflict ->
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "🚨 ${conflict.reason}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF92400E)
                                                )
                                                Text(
                                                    text = "OS #${conflict.targetSession.id} (${conflict.targetSession.time}) ↔ OS #${conflict.conflictingSession.id} (${conflict.conflictingSession.time})",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: SLA Monitor List
                    item {
                        Text(
                            text = "⏱️ Monitoramento em Tempo Real de Tolerância & Regras de SLA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(sessions) { session ->
                        val slaCategory = viewModel.calculateSlaCategory(session)
                        val (bgColor, borderColor, statusLabel, icon) = when (slaCategory) {
                            SlaCategory.PONTUAL -> Quadruple(Color(0xFFECFDF5), Color(0xFFA7F3D0), "🟢 PONTUAL / PRESENÇA REGISTRADA", Icons.Default.CheckCircle)
                            SlaCategory.EM_TOLERANCIA -> Quadruple(Color(0xFFFEF3C7), Color(0xFFFDE68A), "🟡 DENTRO DA TOLERÂNCIA DE 15 MIN", Icons.Default.Schedule)
                            SlaCategory.SLA_ESTOURADO -> Quadruple(Color(0xFFFEF2F2), Color(0xFFFCA5A5), "🔴 SLA ESTOURADO (>15 MIN DE ATRASO)", Icons.Default.Error)
                            SlaCategory.AGUARDANDO -> Quadruple(Color(0xFFF8FAFC), Color(0xFFE2E8F0), "⚪ AGUARDANDO HORÁRIO AGENDADO", Icons.Default.HourglassEmpty)
                            SlaCategory.CONCLUIDO -> Quadruple(Color(0xFFF0FDF4), Color(0xFFBBF7D0), "✅ SERVIÇO CONCLUÍDO E ENCERRADO", Icons.Default.Verified)
                            else -> Quadruple(Color(0xFFF8FAFC), Color(0xFFE2E8F0), "⚪ AGUARDANDO", Icons.Default.HourglassEmpty)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                                        Text(text = statusLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Text(text = "OS #${session.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Text(
                                    text = "${session.company} - ${session.storeName} | Agendado: ${session.time} (${session.date})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "Prestador: ${session.prestadorName} (${session.prestadorPhone}) | Gerente: ${session.gerenteName}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (slaCategory == com.example.data.SlaCategory.SLA_ESTOURADO) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                val msg = "🚨 ALERTA DE SLA ESTOURADO - OS #${session.id}\n\nOlá, ${session.prestadorName}! O horário agendado (${session.time}) já ultrapassou a tolerância regulamentar de 15 minutos sem o seu registro de chegada no link web. Por favor, acesse o link para confirmar sua chegada imediatamente:\n${session.getPrestadorLink()}"
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=55${session.prestadorPhone.replace(Regex("[^0-9]"), "")}&text=${Uri.encode(msg)}"))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    ToastUtils.show(context, "Cobrança enviada ao prestador")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cobrar Prestador Via WhatsApp", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Smart Batch Reminders Dispatch
                            sessions.filter { it.status == "PENDENTE" }.forEach { s ->
                                viewModel.nudgeParticipant(s.id, "PRESTADOR", s.prestadorName, "Lembrete de compromisso agendado para ${s.time}. Acesse o link para check-in.", "AUTOMATICO_SLA")
                            }
                            ToastUtils.show(context, "📱 Disparo de Lembretes Automáticos efetuado para todas as OSs Pendentes!")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disparar Lembretes em Lote", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Fechar Painel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseIntegrationDialog(
    sessions: List<ServiceSession>,
    viewModel: SessionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var testResultStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isSyncingAll by remember { mutableStateOf(false) }
    var tablesList by remember { mutableStateOf<List<SupabaseTableRecord>>(emptyList()) }
    var showSqlSchemaDialog by remember { mutableStateOf(false) }

    val supabaseUrl = viewModel.getSupabaseUrl()
    val isRealConfigured = viewModel.isSupabaseRealConfigured()
    val syncedCount = sessions.count { it.isSyncedToSupabase }

    LaunchedEffect(Unit) {
        viewModel.getSupabaseTablesOverview { list ->
            tablesList = list
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Supabase Branded Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3ECF8E).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "⚡ Supabase Cloud Database",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Surface(
                                    color = Color(0xFFD1FAE5),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isRealConfigured) "PROD LIVE" else "INTEGRADO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF047857),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Sincronização em Tempo Real de OSs e Auditoria em Nuvem",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connection Config Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Text("Endpoint Supabase REST:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF065F46))
                            }
                            Text(
                                text = "$syncedCount/${sessions.size} OSs Sincronizadas",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }

                        Text(
                            text = supabaseUrl,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF047857)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isTestingConnection = true
                                    viewModel.testSupabaseConnection { success, message ->
                                        isTestingConnection = false
                                        testResultStatus = message
                                        ToastUtils.show(context, message)
                                    }
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF059669))
                                } else {
                                    Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Testar Conexão", fontSize = 10.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    isSyncingAll = true
                                    viewModel.syncAllSessionsToSupabase(sessions) { count ->
                                        isSyncingAll = false
                                        ToastUtils.show(context, "✅ $count OSs sincronizadas com sucesso no Supabase!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1.2f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                if (isSyncingAll) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar em Lote", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (testResultStatus != null) {
                            Text(
                                text = "Status: $testResultStatus",
                                fontSize = 10.sp,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List & Schema Options
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 Tabelas & Mapeamento PostgreSQL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            TextButton(onClick = { showSqlSchemaDialog = true }) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver SQL DDL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(tablesList) { tbl ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("public.${tbl.tableName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                                    Text("Segurança: ${tbl.rlsStatus}", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                                Surface(
                                    color = Color(0xFFEEF2FF),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${tbl.recordCount} registros",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📋 Status de Sincronização por OS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(sessions) { session ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (session.isSyncedToSupabase) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (session.isSyncedToSupabase) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("OS #${session.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Surface(
                                            color = if (session.isSyncedToSupabase) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (session.isSyncedToSupabase) "☁️ CLOUD SYNCED" else "⏳ PENDENTE NUVEM",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (session.isSyncedToSupabase) Color(0xFF047857) else Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${session.company} - ${session.storeName} (${session.prestadorName})",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.syncSessionToSupabase(session.id) { success ->
                                            if (success) ToastUtils.show(context, "OS #${session.id} enviada para Supabase!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (session.isSyncedToSupabase) Color(0xFF059669) else Color(0xFFD97706)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (session.isSyncedToSupabase) "Re-Sincronizar" else "Enviar",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar Painel Supabase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSqlSchemaDialog) {
        AlertDialog(
            onDismissRequest = { showSqlSchemaDialog = false },
            title = { Text("⚡ SQL DDL Schema Supabase", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Execute o comando abaixo no Editor SQL do seu projeto Supabase para criar a tabela com RLS ativo:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = """
                            CREATE TABLE public.service_sessions (
                              id bigint PRIMARY KEY,
                              company text,
                              store_name text,
                              address text,
                              date text,
                              time text,
                              description text,
                              service_type text,
                              prestador_name text,
                              prestador_phone text,
                              gerente_name text,
                              gerente_phone text,
                              status text,
                              created_at bigint,
                              synced_at bigint
                            );

                            ALTER TABLE public.service_sessions ENABLE ROW LEVEL SECURITY;
                            CREATE POLICY "Allow public read" ON public.service_sessions FOR SELECT USING (true);
                            """.trimIndent(),
                            fontSize = 9.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSqlSchemaDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


