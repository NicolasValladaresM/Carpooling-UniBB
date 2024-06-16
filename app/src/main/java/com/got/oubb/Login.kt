package com.got.oubb

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val emailText: TextView = findViewById(R.id.textEmail)
        val pwdText: TextView = findViewById(R.id.textPwd)
        val btnIng: Button = findViewById(R.id.ingresar)
        val btnNew: Button = findViewById(R.id.regist)

        emailText.filters = arrayOf(InputFilter.LengthFilter(100))
        pwdText.filters = arrayOf(InputFilter.LengthFilter(50))

        firebaseAuth = Firebase.auth
        val user = firebaseAuth.currentUser

        btnNew.setOnClickListener {

            val i = Intent(this, Regist::class.java)
            startActivity(i)
            finish()
        }



        if (user?.isEmailVerified == true){

            if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                      finish()
            }
    }

        btnIng.setOnClickListener {

            if (emailText.text.isNotEmpty() && pwdText.text.isNotEmpty()) {

                signIn(emailText.text.toString(), pwdText.text.toString())

            }
        }


    }


    private fun signIn(email: String, password: String){

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){ task->

            if(task.isSuccessful){ //si es correcto hace un intent
                val user = firebaseAuth.currentUser


                if(user?.isEmailVerified==true){

                    val i = Intent(this, SeleccionRol::class.java )
                    startActivity(i)
                    finish()

                }else{
                    Toast.makeText(this, "No se ha verificado el correo electrónico", Toast.LENGTH_SHORT).show()

                }


            }else{

                Toast.makeText(this, "Correo o clave erroneos", Toast.LENGTH_SHORT).show()

            }
        }

    }


}