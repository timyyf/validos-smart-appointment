package com.example.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
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
        return sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: ServiceSession) {
        sessionDao.updateSession(session)
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
