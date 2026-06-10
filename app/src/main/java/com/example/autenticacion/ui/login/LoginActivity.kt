package com.example.autenticacion.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.widget.Toast
import com.example.autenticacion.databinding.ActivityLoginBinding

import com.example.autenticacion.R
import com.example.autenticacion.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private var semail: String = ""
    private var spassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            binding.login.setOnClickListener {
                if (validateFields()) {
                    binding.loading.visibility = View.VISIBLE
                    loginUser()
                }
            }

            binding.register.setOnClickListener {
                if (validateFields()) {
                    binding.loading.visibility = View.VISIBLE
                    registerUser()
                }
            }
        }
    }

    private fun validateFields(): Boolean {

        semail = binding.username.text.toString().trim()
        spassword = binding.password.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(semail).matches()) {
            binding.username.error = "Formato inválido de email"
            return false
        }

        if (TextUtils.isEmpty(spassword) || spassword.length < 6) {
            binding.password.error =
                "El password debe tener al menos 6 caracteres"
            return false
        }

        return true
    }

    private fun loginUser() {
        mAuth.signInWithEmailAndPassword(semail, spassword)
            .addOnCompleteListener(this) { task ->

                binding.loading.visibility = View.GONE

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("EMAIL", semail)
                    startActivity(intent)

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Correo o contraseña incorrectos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun registerUser() {
        mAuth.createUserWithEmailAndPassword(semail, spassword)
            .addOnCompleteListener(this) { task ->

                binding.loading.visibility = View.GONE

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Registro exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("EMAIL", semail)
                    startActivity(intent)

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}