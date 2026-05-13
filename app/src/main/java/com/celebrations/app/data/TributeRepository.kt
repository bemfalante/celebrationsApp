package com.celebrations.app.data

import com.celebrations.app.model.Tribute
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TributeRepository(private val tributeDao: TributeDao) {

    val allTributes: Flow<List<Tribute>> = tributeDao.getAllTributes()

    suspend fun refreshTributes() {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("tributes")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val tribute = doc.toObject(Tribute::class.java)?.copy(id = doc.id)
                if (tribute != null) {
                    val existing = tributeDao.getTributeById(tribute.id)
                    if (existing == null) {
                        tributeDao.insertTribute(tribute)
                    } else {
                        // Update metadata but keep local path if already downloaded
                        tributeDao.updateTribute(tribute.copy(
                            localPath = existing.localPath,
                            isDownloaded = existing.isDownloaded
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateTribute(tribute: Tribute) {
        tributeDao.updateTribute(tribute)
    }

    suspend fun getPendingDownloads(): List<Tribute> {
        return tributeDao.getPendingDownloads()
    }
}
