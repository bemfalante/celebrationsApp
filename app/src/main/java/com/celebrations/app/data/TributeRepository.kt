package com.celebrations.app.data

import com.celebrations.app.model.Tribute
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TributeRepository(private val tributeDao: TributeDao) {

    private val client = OkHttpClient()
    private val gson = Gson()

    val allTributes: Flow<List<Tribute>> = tributeDao.getAllTributes()

    suspend fun refreshTributes() = withContext(Dispatchers.IO) {
        if (SupabaseConfig.URL == "YOUR_SUPABASE_URL") return@withContext

        val request = Request.Builder()
            .url("${SupabaseConfig.URL}/rest/v1/${SupabaseConfig.TABLE_NAME}?select=*")
            .addHeader("apikey", SupabaseConfig.ANON_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext

                val body = response.body?.string() ?: return@withContext
                val type = object : TypeToken<List<Tribute>>() {}.type
                val tributes: List<Tribute> = gson.fromJson(body, type)

                for (tribute in tributes) {
                    val existing = tributeDao.getTributeById(tribute.id)
                    if (existing == null) {
                        tributeDao.insertTribute(tribute)
                    } else {
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
