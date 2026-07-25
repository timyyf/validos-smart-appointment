package com.example.ui

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SessionRepository
    
    // UI State
    val allSessions: StateFlow<List<ServiceSession>>
    val allAuditLogs: StateFlow<List<AuditLog>>
    
    private val _currentRole = MutableStateFlow("ADMINISTRADOR") // ADMINISTRADOR, PRESTADOR, GERENTE
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    val selectedSessionId: StateFlow<Long?> = _selectedSessionId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<String?>("TODOS") // TODOS, PENDENTE, INICIADA, FINALIZADA, ENCERRADA
    val filterStatus: StateFlow<String?> = _filterStatus.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SessionRepository(database.sessionDao())
        
        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allAuditLogs = repository.allAuditLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed some sample data if the database is empty
        viewModelScope.launch {
            repository.allSessions.first().let { sessions ->
                if (sessions.isEmpty()) {
                    seedData()
                }
            }
        }
    }

    // Role switcher
    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun selectSession(id: Long?) {
        _selectedSessionId.value = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String?) {
        _filterStatus.value = status
    }

    // Create session
    fun createSession(
        company: String,
        storeName: String,
        address: String,
        date: String,
        time: String,
        description: String,
        serviceType: String,
        prestadorName: String,
        prestadorCompany: String,
        prestadorPhone: String,
        prestadorEmail: String,
        gerenteName: String,
        gerentePhone: String,
        gerenteEmail: String,
        observations: String,
        emitOS: Boolean
    ) {
        viewModelScope.launch {
            val session = ServiceSession(
                company = company,
                storeName = storeName,
                address = address,
                date = date,
                time = time,
                description = description,
                serviceType = serviceType,
                prestadorName = prestadorName,
                prestadorCompany = prestadorCompany,
                prestadorPhone = prestadorPhone,
                prestadorEmail = prestadorEmail,
                gerenteName = gerenteName,
                gerentePhone = gerentePhone,
                gerenteEmail = gerenteEmail,
                observations = observations,
                emitOS = emitOS,
                status = "PENDENTE"
            )
            val newId = repository.insertSession(session)
            repository.logAction(
                sessionId = newId,
                userProfile = "ADMINISTRADOR",
                action = "Sessão criada para a loja $storeName. Status: PENDENTE",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Register Arrival specifically via Web Link Icon ("JÁ CHEGUEI AO LOCAL!")
    fun registerArrivalViaWebLink(sessionId: Long, participantRole: String) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val nowMs = System.currentTimeMillis()
            val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(nowMs))

            val participantName = if (participantRole == "PRESTADOR") session.prestadorName else session.gerenteName
            val updatedSession = if (participantRole == "PRESTADOR") {
                session.copy(prestadorArrivedAt = nowMs)
            } else {
                session.copy(gerenteArrivedAt = nowMs)
            }

            repository.updateSession(updatedSession)

            // Emit notification message to ALL THREE participants (ADM, Prestador, Gerente)
            val broadcastLog = "📍 CHEGADA REGISTRADA VIA LINK WEB: $participantRole ($participantName) clicou 'JÁ CHEGUEI AO LOCAL!' às $timeStr para OS #${session.id}.\n" +
                    "📩 NOTIFICAÇÃO ENVIADA AOS 3 ENVOLVEDOS:\n" +
                    "• ADM (Painel Geral)\n" +
                    "• PRESTADOR (${session.prestadorName} - ${session.prestadorPhone})\n" +
                    "• GERENTE (${session.gerenteName} - ${session.gerentePhone})"

            repository.logAction(
                sessionId = sessionId,
                userProfile = participantRole,
                action = broadcastLog,
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Prestador: Confirm presence and fill profile details
    fun confirmPrestadorPresence(
        sessionId: Long,
        cpf: String,
        rg: String,
        cargo: String,
        email: String,
        docUri: String,
        versoUri: String?
    ) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val nowMs = System.currentTimeMillis()
            
            val updatedSession = session.copy(
                prestadorCpf = cpf,
                prestadorRg = rg,
                prestadorCargo = cargo,
                prestadorEmail = email,
                prestadorDocumentUri = docUri,
                prestadorVersoUri = versoUri,
                prestadorConfirmedAt = nowMs,
                prestadorArrivedAt = session.prestadorArrivedAt ?: nowMs,
                prestadorIp = getMockIpAddress(),
                prestadorDevice = getMockDeviceName(),
                prestadorNavegador = "Chrome Mobile v126",
                prestadorAcceptedTerms = true,
                prestadorAcceptedLgpd = true
            )

            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "PRESTADOR",
                action = "Prestador confirmou agendamento e enviou identificação civil (LGPD Consentida)",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
            
            checkAndStartSession(sessionId)
        }
    }

    // Gerente: Confirm presence and authorize provider entry
    fun confirmGerentePresence(
        sessionId: Long,
        osNumber: String?
    ) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val nowMs = System.currentTimeMillis()
            
            val updatedSession = session.copy(
                gerenteConfirmedAt = nowMs,
                gerenteArrivedAt = session.gerenteArrivedAt ?: nowMs,
                gerenteIp = getMockIpAddress(),
                gerenteDevice = getMockDeviceName(),
                gerenteOsEmitted = !osNumber.isNullOrBlank(),
                gerenteOsNumber = osNumber
            )

            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "GERENTE",
                action = "Gerente autorizou a entrada e confirmou recepção no local" + 
                         if (!osNumber.isNullOrBlank()) " (OS Emitida: $osNumber)" else "",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )

            checkAndStartSession(sessionId)
        }
    }

    // Main Rule: Start Session if both confirm
    private suspend fun checkAndStartSession(sessionId: Long) {
        val session = repository.getSessionByIdSuspend(sessionId) ?: return
        
        val ready = session.prestadorConfirmedAt != null && session.gerenteConfirmedAt != null
        if (ready && session.status == "PENDENTE") {
            val hashValue = generateHash("COMPROMISSO-${session.id}-${System.currentTimeMillis()}")
            
            val updatedSession = session.copy(
                status = "INICIADA",
                hashCompromisso = hashValue,
                pdfCompromissoGeneratedAt = System.currentTimeMillis()
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "SISTEMA",
                action = "SESSÃO INICIADA AUTOMATICAMENTE. PDF 1 (Compromisso) gerado com Hash: $hashValue",
                ipAddress = "127.0.0.1",
                device = "ValidOS Core Engine"
            )
        }
    }

    // Admin: Generate Web Access Link
    fun generateWebLink(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val updatedSession = session.copy(webLinkGerado = true)
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Administrador gerou e publicou o Link Web de acesso compartilhado para Prestador e Gerente.",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Force start or manually validate session
    fun forceStartSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val hashValue = generateHash("MANUAL-${session.id}-${System.currentTimeMillis()}")
            
            val updatedSession = session.copy(
                status = "INICIADA",
                hashCompromisso = hashValue,
                pdfCompromissoGeneratedAt = System.currentTimeMillis(),
                // Fill details if missing
                prestadorConfirmedAt = session.prestadorConfirmedAt ?: System.currentTimeMillis(),
                prestadorIp = session.prestadorIp ?: getMockIpAddress(),
                prestadorDevice = session.prestadorDevice ?: getMockDeviceName(),
                gerenteConfirmedAt = session.gerenteConfirmedAt ?: System.currentTimeMillis(),
                gerenteIp = session.gerenteIp ?: getMockIpAddress(),
                gerenteDevice = session.gerenteDevice ?: getMockDeviceName()
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Administrador validou manualmente e iniciou a sessão. PDF 1 (Compromisso) gerado com Hash: $hashValue",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Register attendance on site
    fun registerAttendance(sessionId: Long, prestadorPresent: Boolean, gerentePresent: Boolean) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            
            val updatedSession = session.copy(
                compareceuPrestador = prestadorPresent,
                compareceuGerente = gerentePresent
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Registro de comparecimento atualizado: Prestador ${if (prestadorPresent) "COMPARECEU" else "FALTOU"}, Gerente ${if (gerentePresent) "COMPARECEU" else "FALTOU"}",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Release Technical Report
    fun releaseTechnicalReport(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            
            val updatedSession = session.copy(
                status = "RELATORIO_LIBERADO"
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Administrador liberou o preenchimento do Relatório Técnico Final",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Nudge/Remind a participant regarding critical pre-requisites & log unfulfilled schedule
    fun nudgeParticipant(sessionId: Long, participantRole: String, participantName: String, actionRequired: String, channel: String = "SISTEMA") {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "⚠️ HORÁRIO ACORDADO NÃO CUMPRIDO ($participantRole $participantName). Cobrança enviada via $channel. A OS #${session.id} permanece aberta aguardando a chegada da parte pendente.",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Prestador: Fill and submit Technical Report
    fun submitTechnicalReport(
        sessionId: Long,
        description: String,
        materials: String,
        equipments: String,
        pieces: String,
        duration: String,
        obs: String,
        isCompleted: Boolean,
        reason: String?,
        photoUri: String?,
        signatureUri: String?
    ) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            
            val updatedSession = session.copy(
                status = "FINALIZADA",
                relatorioDescricao = description,
                relatorioMateriais = materials,
                relatorioEquipamentos = equipments,
                relatorioPecas = pieces,
                relatorioTempo = duration,
                relatorioObservacoes = obs,
                relatorioConcluido = isCompleted,
                relatorioMotivo = if (!isCompleted) reason else null,
                relatorioFotoUri = photoUri,
                relatorioSignatureUri = signatureUri
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "PRESTADOR",
                action = "Relatório Técnico finalizado e assinado digitalmente pelo prestador. Status: FINALIZADA",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Review and Close Session
    fun closeSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            val hashValue = generateHash("FINAL-${session.id}-${System.currentTimeMillis()}")
            
            val updatedSession = session.copy(
                status = "ENCERRADA",
                hashFinal = hashValue,
                pdfFinalGeneratedAt = System.currentTimeMillis()
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Administrador revisou, encerrou e lacrou a sessão de serviço. PDF 2 (Execução Final) gerado com Hash: $hashValue",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Cancel Session
    fun cancelSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            
            val updatedSession = session.copy(
                status = "CANCELADA"
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Sessão de serviço CANCELADA pelo administrador e links desativados",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Reopen Session if closed or canceled by mistake
    fun reopenSession(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionByIdSuspend(sessionId) ?: return@launch
            
            val updatedSession = session.copy(
                status = "INICIADA"
            )
            
            repository.updateSession(updatedSession)
            repository.logAction(
                sessionId = sessionId,
                userProfile = "ADMINISTRADOR",
                action = "Administrador REABRIU a sessão de serviço. Links web reativados.",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Admin: Register a custom audit note / contact attempt
    fun logCustomAction(sessionId: Long, note: String) {
        viewModelScope.launch {
            repository.logAction(
                sessionId = sessionId,
                userProfile = _currentRole.value,
                action = "AUDITORIA ADM: $note",
                ipAddress = getMockIpAddress(),
                device = getMockDeviceName()
            )
        }
    }

    // Helpers to mock client telemetry
    private fun getMockIpAddress(): String {
        return "177.89.244." + (10..254).random()
    }

    private fun getMockDeviceName(): String {
        val brands = listOf("Samsung Galaxy S24 Ultra", "Motorola Edge 50 Neo", "Xiaomi 14 Pro", "Google Pixel 8 Pro", "iPhone 15 Pro Max")
        return brands.random()
    }

    private fun generateHash(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(32).uppercase()
        } catch (e: Exception) {
            "HASH" + UUID.randomUUID().toString().take(12).uppercase()
        }
    }

    // Seeding sample data
    private suspend fun seedData() {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val today = df.format(Date())
        val yesterday = df.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        
        val s1 = ServiceSession(
            company = "Supermercados Carrefour",
            storeName = "Carrefour - Unidade Pinheiros",
            address = "Av. das Nações Unidas, 15187 - Pinheiros, São Paulo - SP",
            date = today,
            time = "10:00",
            description = "Manutenção preventiva das câmaras frias de congelados e resfriados.",
            serviceType = "Refrigeração",
            prestadorName = "Marcos Silva",
            prestadorCompany = "RefriMax Soluções Térmicas",
            prestadorPhone = "(11) 98888-7777",
            prestadorEmail = "marcos@refrimax.com.br",
            gerenteName = "Carlos Eduardo",
            gerentePhone = "(11) 97777-6666",
            gerenteEmail = "carlos.eduardo@carrefour.com",
            observations = "Necessário levar EPIs completos (luvas térmicas, botas de proteção e casaco térmico).",
            emitOS = true,
            status = "PENDENTE"
        )
        val s1Id = repository.insertSession(s1)
        repository.logAction(s1Id, "ADMINISTRADOR", "Sessão criada e aguardando confirmações de presença.", "189.44.12.101", "Lenovo ThinkPad T14 (Chrome)")

        val s2 = ServiceSession(
            company = "Lojas Americanas",
            storeName = "Americanas - Shopping Ibirapuera",
            address = "Av. Ibirapuera, 3103 - Indianópolis, São Paulo - SP",
            date = today,
            time = "14:00",
            description = "Substituição das lâmpadas fluorescentes por painéis de LED no estoque e salão principal.",
            serviceType = "Elétrica",
            prestadorName = "Roberto Almeida",
            prestadorCompany = "Almeida Instalações Elétricas",
            prestadorPhone = "(11) 96666-5555",
            prestadorEmail = "roberto@almeidaeletrica.com",
            gerenteName = "Fernanda Costa",
            gerentePhone = "(11) 95555-4444",
            gerenteEmail = "fernanda.costa@americanas.com",
            observations = "Trabalho em altura. Prestador deve apresentar certificado NR-35.",
            emitOS = true,
            status = "INICIADA",
            prestadorCpf = "123.456.789-00",
            prestadorRg = "12.345.678-9",
            prestadorCargo = "Eletricista Sênior",
            prestadorDocumentUri = "simulated_id_card.png",
            prestadorConfirmedAt = System.currentTimeMillis() - 3600000,
            prestadorIp = "177.100.22.41",
            prestadorDevice = "Samsung Galaxy A54",
            prestadorNavegador = "Chrome Mobile v125",
            prestadorAcceptedTerms = true,
            prestadorAcceptedLgpd = true,
            gerenteConfirmedAt = System.currentTimeMillis() - 3000000,
            gerenteIp = "201.55.88.92",
            gerenteDevice = "iPhone 14 Pro",
            gerenteOsEmitted = true,
            gerenteOsNumber = "OS-2026-9912",
            hashCompromisso = generateHash("COMP-SEED-2"),
            pdfCompromissoGeneratedAt = System.currentTimeMillis() - 3000000
        )
        val s2Id = repository.insertSession(s2)
        repository.logAction(s2Id, "ADMINISTRADOR", "Sessão de serviço criada", "189.44.12.101", "Lenovo ThinkPad T14")
        repository.logAction(s2Id, "PRESTADOR", "Prestador Marcos confirmou presença via celular", "177.100.22.41", "Samsung Galaxy A54")
        repository.logAction(s2Id, "GERENTE", "Gerente Fernanda confirmou presença no local e emitiu a OS-2026-9912", "201.55.88.92", "iPhone 14 Pro")
        repository.logAction(s2Id, "SISTEMA", "SESSÃO INICIADA AUTOMATICAMENTE. PDF 1 (Compromisso) gerado", "127.0.0.1", "ValidOS Engine")

        val s3 = ServiceSession(
            company = "Drogaria São Paulo",
            storeName = "Drogaria SP - Av. Paulista 1000",
            address = "Av. Paulista, 1000 - Bela Vista, São Paulo - SP",
            date = yesterday,
            time = "16:00",
            description = "Manutenção do ar condicionado central e troca dos filtros purificadores.",
            serviceType = "Climatização",
            prestadorName = "Julio Cesar",
            prestadorCompany = "ClimaClean Paulista",
            prestadorPhone = "(11) 94444-3333",
            prestadorEmail = "julio@climaclean.com.br",
            gerenteName = "Ricardo Souza",
            gerentePhone = "(11) 93333-2222",
            gerenteEmail = "ricardo.souza@drogariasp.com.br",
            observations = "Trabalho após o horário de fechamento.",
            emitOS = false,
            status = "ENCERRADA",
            prestadorCpf = "987.654.321-11",
            prestadorRg = "98.765.432-1",
            prestadorCargo = "Técnico Ar Condicionado",
            prestadorDocumentUri = "simulated_id_paulista.png",
            prestadorConfirmedAt = System.currentTimeMillis() - 86400000,
            prestadorIp = "179.44.33.11",
            prestadorDevice = "Motorola Edge 40",
            prestadorNavegador = "Firefox Mobile v120",
            prestadorAcceptedTerms = true,
            prestadorAcceptedLgpd = true,
            gerenteConfirmedAt = System.currentTimeMillis() - 86300000,
            gerenteIp = "200.200.5.5",
            gerenteDevice = "Samsung S23",
            compareceuPrestador = true,
            compareceuGerente = true,
            relatorioDescricao = "Limpeza completa do duto principal, condensadoras externas higienizadas e substituição dos filtros purificadores.",
            relatorioMateriais = "Pastilha sanitizante (2 unid), Filtros HEPA (3 unid)",
            relatorioEquipamentos = "Bomba de pressurização, Escada, Aspirador industrial",
            relatorioPecas = "Filtro HEPA SP-30",
            relatorioTempo = "2h 30m",
            relatorioObservacoes = "Equipamento funcionando perfeitamente após a manutenção. Vazões normalizadas.",
            relatorioConcluido = true,
            relatorioFotoUri = "attached_clima.png",
            relatorioSignatureUri = "simulated_signature_julio",
            hashCompromisso = generateHash("COMP-SEED-3"),
            pdfCompromissoGeneratedAt = System.currentTimeMillis() - 86400000,
            hashFinal = generateHash("FINAL-SEED-3"),
            pdfFinalGeneratedAt = System.currentTimeMillis() - 80000000
        )
        val s3Id = repository.insertSession(s3)
        repository.logAction(s3Id, "ADMINISTRADOR", "Sessão criada no sistema", "189.44.12.101", "Lenovo ThinkPad T14")
        repository.logAction(s3Id, "PRESTADOR", "Prestador confirmou agendamento", "179.44.33.11", "Motorola Edge 40")
        repository.logAction(s3Id, "GERENTE", "Gerente autorizou a entrada e recepção", "200.200.5.5", "Samsung S23")
        repository.logAction(s3Id, "SISTEMA", "SESSÃO INICIADA. PDF 1 de Compromisso assinado", "127.0.0.1", "ValidOS Engine")
        repository.logAction(s3Id, "ADMINISTRADOR", "Comparecimento validado no local: Prestador PRESENTE, Gerente PRESENTE", "189.44.12.101", "Lenovo ThinkPad")
        repository.logAction(s3Id, "ADMINISTRADOR", "Relatório final liberado para preenchimento", "189.44.12.101", "Lenovo ThinkPad")
        repository.logAction(s3Id, "PRESTADOR", "Relatório de execução submetido e assinado digitalmente", "179.44.33.11", "Motorola Edge 40")
        repository.logAction(s3Id, "ADMINISTRADOR", "Revisão efetuada e Sessão ENCERRADA E LACRADA. PDF 2 gerado.", "189.44.12.101", "Lenovo ThinkPad")
    }

    // Smart Appointment System Functions
    fun calculateSlaCategory(session: ServiceSession): SlaCategory {
        if (session.status == "FINALIZADA" || session.status == "ENCERRADA") {
            return SlaCategory.CONCLUIDO
        }
        
        if (session.prestadorArrivedAt != null || session.prestadorConfirmedAt != null) {
            return SlaCategory.PONTUAL
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            val sessionDateTime = sdf.parse("${session.date} ${session.time}")
            if (sessionDateTime != null) {
                val now = System.currentTimeMillis()
                val sessionMs = sessionDateTime.time
                val diffMinutes = (now - sessionMs) / (1000 * 60)
                
                when {
                    diffMinutes < 0 -> SlaCategory.AGUARDANDO
                    diffMinutes in 0..15 -> SlaCategory.EM_TOLERANCIA
                    else -> SlaCategory.SLA_ESTOURADO
                }
            } else {
                SlaCategory.AGUARDANDO
            }
        } catch (e: Exception) {
            SlaCategory.AGUARDANDO
        }
    }

    fun detectScheduleConflicts(session: ServiceSession, allSessions: List<ServiceSession>): List<SmartConflict> {
        val conflicts = mutableListOf<SmartConflict>()
        for (other in allSessions) {
            if (other.id == session.id) continue
            
            if (other.date == session.date) {
                if (other.prestadorName.equals(session.prestadorName, ignoreCase = true) && other.time == session.time) {
                    conflicts.add(
                        SmartConflict(
                            targetSession = session,
                            conflictingSession = other,
                            reason = "Prestador ${session.prestadorName} agendado simultaneamente na OS #${other.id} (${other.storeName})"
                        )
                    )
                }
                
                if (other.storeName.equals(session.storeName, ignoreCase = true) && other.time == session.time) {
                    conflicts.add(
                        SmartConflict(
                            targetSession = session,
                            conflictingSession = other,
                            reason = "Loja (${session.storeName}) possui múltiplos atendimentos agendados às ${session.time} (OS #${other.id})"
                        )
                    )
                }
            }
        }
        return conflicts
    }

    fun getSmartAppointmentStats(sessions: List<ServiceSession>): SmartAppointmentStats {
        val total = sessions.size
        val pending = sessions.count { it.status == "PENDENTE" }
        val completed = sessions.count { it.status == "FINALIZADA" || it.status == "ENCERRADA" }
        
        var slaViolations = 0
        var punctualCount = 0
        var totalConflicts = 0

        for (s in sessions) {
            val cat = calculateSlaCategory(s)
            if (cat == SlaCategory.SLA_ESTOURADO) slaViolations++
            if (cat == SlaCategory.PONTUAL || cat == SlaCategory.CONCLUIDO) punctualCount++

            val c = detectScheduleConflicts(s, sessions)
            if (c.isNotEmpty()) totalConflicts += c.size
        }

        val punctualityRate = if (total > 0) (punctualCount * 100) / total else 100

        return SmartAppointmentStats(
            totalSessions = total,
            pendingSessions = pending,
            completedSessions = completed,
            slaViolationsCount = slaViolations,
            punctualityRatePercent = punctualityRate,
            conflictsDetectedCount = totalConflicts
        )
    }

    // Supabase Cloud Integration Functions
    fun getSupabaseUrl(): String = repository.supabaseService.getSupabaseUrl()

    fun isSupabaseRealConfigured(): Boolean = repository.supabaseService.isRealConfigured()

    fun syncSessionToSupabase(sessionId: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.syncSessionToSupabase(sessionId)
            if (success) {
                repository.logAction(sessionId, "ADMINISTRADOR", "Sessão sincronizada com o Supabase Cloud (PostgreSQL)", "177.12.88.90", "Android App")
            }
            onResult(success)
        }
    }

    fun syncAllSessionsToSupabase(sessions: List<ServiceSession>, onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.syncAllSessionsToSupabase(sessions)
            if (count > 0) {
                repository.logAction(1, "ADMINISTRADOR", "Sincronização em lote efetuada no Supabase ($count OSs enviadas)", "177.12.88.90", "Android App")
            }
            onResult(count)
        }
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (success, message) = repository.testSupabaseConnection()
            onResult(success, message)
        }
    }

    fun getSupabaseTablesOverview(onResult: (List<SupabaseTableRecord>) -> Unit) {
        viewModelScope.launch {
            val tables = repository.supabaseService.getTablesOverview()
            onResult(tables)
        }
    }
}

