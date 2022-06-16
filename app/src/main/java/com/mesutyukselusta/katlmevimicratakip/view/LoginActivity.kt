package com.mesutyukselusta.katlmevimicratakip.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mesutyukselusta.katlmevimicratakip.R
import com.mesutyukselusta.katlmevimicratakip.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null){
            val intent = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val userName = binding.etUserName.text.toString()
            val pass = binding.etPassword.text.toString()

            signIn(userName,pass)
         }



    }

    private fun signIn(email : String,pass : String) {
        auth.signInWithEmailAndPassword(email,pass).addOnSuccessListener {
            val intent = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        } . addOnFailureListener {
            Toast.makeText(this@LoginActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
        }

    }


}