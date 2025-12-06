package com.example.masterrollerdice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.masterrollerdice.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    // Variable para almacenar la referencia del View Binding
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el diseño utilizando View Binding
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnInicio.setOnClickListener {
            view.findNavController().navigate(R.id.mainActivity)
        }
    }
}