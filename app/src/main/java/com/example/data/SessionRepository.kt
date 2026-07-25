package com.example.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val sessionDao: SessionDao,
    val supabaseService: SupabaseService = SupabaseService()
) {
    val allSessions: Flow<List<ServiceSession>> = sessionDao.getAllSessions()
    val allAuditLogs: Flow<List<AuditLog>> = sessionDao.getAllAuditLogs()

    fun getSessionById(id: Long): Flow<ServiceSession?> {
        return sessionDao.getSessionById(id)
    }

    suspend fun getSessionByIdSuspend(id: Long): ServiceSession? {
        return sessionDao.getSessionByIdSuspend(id)
    }

    fun getAuditLogsForSession(sessionId: Long): Flow<List<AuditLog>> {
        return sessionDao.getAuditLogsForSession(sessionId)
    }

    suspend fun insertSession(session: ServiceSession): Long {
        val id = sessionDao.insertSession(session)
        val created = sessionDao.getSessionByIdSuspend(id)
        if (created != null) {
            val synced = supabaseService.syncSessionToCloud(created)
            if (synced) {
                sessionDao.updateSession(created.copy(isSyncedToSupabase = true, supabaseSyncedAt = System.currentTimeMillis()))
            }
        }
        return id
    }

    suspend fun updateSession(session: ServiceSession) {
        sessionDao.updateSession(session)
    }

    suspend fun syncSessionToSupabase(sessionId: Long): Boolean {
        val session = sessionDao.getSessionByIdSuspend(sessionId) ?: return false
        val success = supabaseService.syncSessionToCloud(session)
        if (success) {
            sessionDao.updateSession(session.copy(isSyncedToSupabase = true, supabaseSyncedAt = System.currentTimeMillis()))
        }
        return success
    }

    suspend fun syncAllSessionsToSupabase(sessions: List<ServiceSession>): Int {
        var count = 0
        for (session in sessions) {
            val success = supabaseService.syncSessionToCloud(session)
            if (success) {
                sessionDao.updateSession(session.copy(isSyncedToSupabase = true, supabaseSyncedAt = System.currentTimeMillis()))
                count++
            }
        }
        return count
    }

    suspend fun testSupabaseConnection(): Pair<Boolean, String> {
        return supabaseService.testConnection()
    }

    suspend fun logAction(sessionId: Long, userProfile: String, action: String, ipAddress: String, device: String) {
        val auditLog = AuditLog(
            sessionId = sessionId,
            userProfile = userProfile,
            action = action,
            ipAddress = ipAddress,
            device = device
        )
        sessionDao.insertAuditLog(auditLog)
    }
}

