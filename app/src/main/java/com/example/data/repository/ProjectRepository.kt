package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.ProjectSerializer
import com.example.data.db.AppDatabase
import com.example.data.db.ProjectEntity
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProjectRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.projectDao()

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun loadProject(id: String): Project? = withContext(Dispatchers.IO) {
        val entity = dao.getProjectById(id) ?: return@withContext null
        ProjectSerializer.deserialize(entity.dataJson).copy(
            id = entity.id,
            title = entity.title,
            width = entity.width,
            height = entity.height,
            fps = entity.fps,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isSynced = entity.isSynced,
            thumbnailPath = entity.thumbnailPath
        )
    }

    suspend fun saveProject(project: Project, previewBitmap: Bitmap? = null) = withContext(Dispatchers.IO) {
        var thumbPath = project.thumbnailPath
        if (previewBitmap != null) {
            try {
                val thumbFile = File(context.filesDir, "thumb_${project.id}.png")
                FileOutputStream(thumbFile).use { out ->
                    previewBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                thumbPath = thumbFile.absolutePath
            } catch (e: Exception) {
                // Ignore preview save failure
            }
        }

        val json = ProjectSerializer.serialize(project)
        val entity = ProjectEntity(
            id = project.id,
            title = project.title,
            width = project.width,
            height = project.height,
            fps = project.fps,
            createdAt = project.createdAt,
            updatedAt = System.currentTimeMillis(),
            isSynced = project.isSynced,
            thumbnailPath = thumbPath,
            dataJson = json
        )
        dao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
        try {
            val thumbFile = File(context.filesDir, "thumb_${id}.png")
            if (thumbFile.exists()) thumbFile.delete()
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun updateSyncStatus(id: String, synced: Boolean) = withContext(Dispatchers.IO) {
        dao.updateSyncStatus(id, synced, System.currentTimeMillis())
    }
}
