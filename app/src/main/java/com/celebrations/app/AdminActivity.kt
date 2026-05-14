package com.celebrations.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.celebrations.app.data.SupabaseConfig
import com.celebrations.app.databinding.ActivityAdminBinding
import com.celebrations.app.model.Tribute
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            val title = binding.editTitle.text.toString()
            val url = binding.editUrl.text.toString()
            val type = when (binding.radioGroupType.checkedRadioButtonId) {
                R.id.radioImage -> "image"
                R.id.radioVideo -> "video"
                else -> "audio"
            }

            if (title.isNotEmpty() && url.isNotEmpty()) {
                val tribute = Tribute(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    date = System.currentTimeMillis(),
                    type = type,
                    fileUrl = url
                )

                uploadToSupabase(tribute)
            }
        }
    }

    private fun uploadToSupabase(tribute: Tribute) {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val json = gson.toJson(tribute)
                    val body = json.toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url("${SupabaseConfig.URL}/rest/v1/${SupabaseConfig.TABLE_NAME}")
                        .post(body)
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                        .addHeader("Prefer", "return=minimal")
                        .build()

                    client.newCall(request).execute().use { it.isSuccessful }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                Toast.makeText(this@AdminActivity, "Sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@AdminActivity, "Erro ao salvar no Supabase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
