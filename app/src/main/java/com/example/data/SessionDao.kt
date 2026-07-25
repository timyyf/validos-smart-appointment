package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM service_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ServiceSession>>

    @Query("SELECT * FROM service_sessions WHERE id = :id")
    fun getSessionById(id: Long): Flow<ServiceSession?>

    @Query("SELECT * FROM service_sessions WHERE id = :id")
    suspend fun getSessionByIdSuspend(id: Long): ServiceSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ServiceSession): Long

    @Update
    suspend fun updateSession(session: ServiceSession)

    @Query("SELECT * FROM audit_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getAuditLogsForSession(sessionId: Long): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>
}
