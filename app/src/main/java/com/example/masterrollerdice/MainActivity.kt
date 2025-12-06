package com.example.masterrollerdice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.masterrollerdice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Variable para almacenar la referencia del View Binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el diseño utilizando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}
