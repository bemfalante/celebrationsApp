package com.celebrations.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.celebrations.app.data.AppDatabase
import com.celebrations.app.data.TributeRepository
import com.celebrations.app.databinding.ActivityMainBinding
import com.celebrations.app.model.Tribute
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val repository: TributeRepository) : ViewModel() {
    val allTributes: LiveData<List<Tribute>> = repository.allTributes.asLiveData()

    fun refresh() {
        viewModelScope.launch {
            repository.refreshTributes()
        }
    }
}

class MainViewModelFactory(private val repository: TributeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(TributeRepository(AppDatabase.getDatabase(this).tributeDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TributeAdapter { tribute ->
            openTribute(tribute)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allTributes.observe(this) { tributes ->
            adapter.submitList(tributes)
            binding.emptyView.visibility = if (tributes.isEmpty()) View.VISIBLE else View.GONE
        }

        // Secret access to admin: long click on empty space (or just for testing)
        binding.root.setOnLongClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
            true
        }

        viewModel.refresh()
    }

    private fun openTribute(tribute: Tribute) {
        val path = tribute.localPath ?: return
        val file = File(path)
        if (!file.exists()) return

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, getContentResolver().getType(uri))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback or message if no app can handle it
        }
    }
}
