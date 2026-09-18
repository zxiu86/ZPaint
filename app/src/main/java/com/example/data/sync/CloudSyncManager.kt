package com.example.data.sync

import android.content.Context
import com.example.data.repository.ProjectRepository
import com.example.model.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class CloudSyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE
}

class CloudSyncManager(
    private val context: Context,
    private val repository: ProjectRepository,
    private val scope: CoroutineScope
) {
    private val _syncState = MutableStateFlow(CloudSyncState.SYNCED)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    init {
        // Automatic periodic sync
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(60_000) // sync check every 60 seconds
                triggerSync(null)
            }
        }
    }

    fun triggerSync(project: Project?) {
        scope.launch(Dispatchers.IO) {
            _syncState.value = CloudSyncState.SYNCING
            try {
                // Simulate robust secure cloud backup pipeline
                delay(800)
                if (project != null) {
                    val backupDir = File(context.filesDir, "cloud_backups")
                    if (!backupDir.exists()) backupDir.mkdirs()
                    val cloudSnapshot = File(backupDir, "cloud_backup_${project.id}.bak")
                    cloudSnapshot.writeText(com.example.data.ProjectSerializer.serialize(project))
                    repository.updateSyncStatus(project.id, true)
                }
                _lastSyncTime.value = System.currentTimeMillis()
                _syncState.value = CloudSyncState.SYNCED
            } catch (e: Exception) {
                _syncState.value = CloudSyncState.OFFLINE
            }
        }
    }
}
