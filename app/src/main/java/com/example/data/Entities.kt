package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_sessions")
data class ServiceSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val company: String,
    val storeName: String,
    val address: String,
    val date: String,
    val time: String,
    val description: String,
    val serviceType: String,
    val prestadorName: String,
    val prestadorCompany: String,
    val prestadorPhone: String,
    val prestadorEmail: String,
    val gerenteName: String,
    val gerentePhone: String,
    val gerenteEmail: String,
    val observations: String,
    val emitOS: Boolean,
    val webLinkGerado: Boolean = true,
    
    // Status can be: PENDENTE, INICIADA, RELATORIO_LIBERADO, FINALIZADA, ENCERRADA, CANCELADA
    val status: String = "PENDENTE",
    
    // Prestador verification & acceptance
    val prestadorCpf: String? = null,
    val prestadorRg: String? = null,
    val prestadorCargo: String? = null,
    val prestadorDocumentUri: String? = null,
    val prestadorVersoUri: String? = null,
    val prestadorConfirmedAt: Long? = null,
    val prestadorArrivedAt: Long? = null,
    val prestadorIp: String? = null,
    val prestadorDevice: String? = null,
    val prestadorNavegador: String? = null,
    val prestadorAcceptedTerms: Boolean = false,
    val prestadorAcceptedLgpd: Boolean = false,
    
    // Gerente validation & acceptance
    val gerenteConfirmedAt: Long? = null,
    val gerenteArrivedAt: Long? = null,
    val gerenteIp: String? = null,
    val gerenteDevice: String? = null,
    val gerenteOsEmitted: Boolean = false,
    val gerenteOsNumber: String? = null,
    
    // Attendance validation (Comparecimento)
    val compareceuPrestador: Boolean? = null, // true = Sim, false = Não
    val compareceuGerente: Boolean? = null,    // true = Sim, false = Não
    
    // Technical Report (Relatório Técnico)
    val relatorioDescricao: String? = null,
    val relatorioMateriais: String? = null,
    val relatorioEquipamentos: String? = null,
    val relatorioPecas: String? = null,
    val relatorioTempo: String? = null,
    val relatorioObservacoes: String? = null,
    val relatorioConcluido: Boolean? = null, // true = Sim, false = Não
    val relatorioMotivo: String? = null, // required if concluido is False
    val relatorioFotoUri: String? = null,
    val relatorioSignatureUri: String? = null, // base64 or simulated path
    
    // PDF Evidences
    val hashCompromisso: String? = null,
    val hashFinal: String? = null,
    val pdfCompromissoGeneratedAt: Long? = null,
    val pdfFinalGeneratedAt: Long? = null,
    
    // Supabase Cloud Sync
    val isSyncedToSupabase: Boolean = false,
    val supabaseSyncedAt: Long? = null,
    
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getPrestadorLink(): String = "https://validos.app/session/$id/prestador"
    fun getGerenteLink(): String = "https://validos.app/session/$id/gerente"
    fun isLinkActive(): Boolean = status != "ENCERRADA" && status != "CANCELADA" && status != "FINALIZADA"
}

data class SupabaseSyncStatus(
    val isConfigured: Boolean,
    val supabaseUrl: String,
    val totalSyncedSessions: Int,
    val lastSyncTimestamp: Long?,
    val activeTables: List<String>,
    val errorMessage: String? = null
)

data class SupabaseTableRecord(
    val tableName: String,
    val recordCount: Int,
    val rlsStatus: String,
    val lastUpdate: String
)

enum class SlaCategory {
    PONTUAL,          // Confirmado/Chegou no horário ou até 15 min
    EM_TOLERANCIA,    // No horário agendado dentro da tolerância de 15 min
    SLA_ESTOURADO,    // Atrasado além de 15 min sem chegada
    AGUARDANDO,       // Agendado para o futuro
    CONCLUIDO         // Sessão finalizada/encerrada
}

data class SmartConflict(
    val targetSession: ServiceSession,
    val conflictingSession: ServiceSession,
    val reason: String // e.g., "Mesmo Prestador em agendamentos simultâneos" or "Conflito de Loja/Local"
)

data class SmartAppointmentStats(
    val totalSessions: Int,
    val pendingSessions: Int,
    val completedSessions: Int,
    val slaViolationsCount: Int,
    val punctualityRatePercent: Int,
    val conflictsDetectedCount: Int
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val userProfile: String, // "ADMINISTRADOR", "PRESTADOR", "GERENTE"
    val action: String,
    val ipAddress: String,
    val device: String,
    val timestamp: Long = System.currentTimeMillis()
)
