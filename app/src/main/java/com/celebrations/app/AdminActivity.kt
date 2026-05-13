package com.celebrations.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.celebrations.app.databinding.ActivityAdminBinding
import com.celebrations.app.model.Tribute
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

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

                FirebaseFirestore.getInstance().collection("tributes")
                    .document(tribute.id)
                    .set(tribute)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
