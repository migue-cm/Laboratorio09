package com.example.autenticacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.autenticacion.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("EMAIL")

        binding.tvWelcome.text = "Bienvenido $email"
    }
}