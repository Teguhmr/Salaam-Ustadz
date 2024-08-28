package id.uinjkt.salaamustadz.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.uinjkt.salaamustadz.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding as ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}