import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ttai.network.ApiService
import com.example.ttai.ui.vm.CollectViewModel
import com.example.ttai.ui.vm.RequestModelViewModel

class CollectViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectViewModel::class.java)) {
            return CollectViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}