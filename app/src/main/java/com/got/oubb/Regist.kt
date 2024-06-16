package com.got.oubb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Regist : AppCompatActivity() {



    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var textNombre:  EditText
    private lateinit var textCar: EditText



    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist)

        auth = FirebaseAuth.getInstance()

        textNombre = findViewById(R.id.textNombre)
        textCar = findViewById(R.id.textCar)


        val btnIng: Button = findViewById(R.id.registButon)
        val btnLog: Button = findViewById(R.id.toLogin)













        val maxLengthNombre = 28
        textNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthNombre) {
                    textNombre.setText(s.subSequence(0, maxLengthNombre))
                    textNombre.setSelection(maxLengthNombre)
                }
            }
        })




        val maxLengthCarrera = 5
        textCar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthCarrera) {
                    textCar.setText(s.subSequence(0, maxLengthCarrera))
                    textCar.setSelection(maxLengthCarrera)
                }
            }
        })









        btnIng.setOnClickListener {

            if (!textNombre.text.isBlank() && !textCar.text.isBlank()){
                registrarUsuario()
            }else{

                Toast.makeText(this, "Debe ingresar un nombre y una carrera", Toast.LENGTH_SHORT).show()

            }




        }
        btnLog.setOnClickListener {
            val i = Intent(this, Login::class.java )
            startActivity(i)
            finish()
        }








    }



    private fun registrarUsuario() {

        val correoText: TextView= findViewById(R.id.textCorreoRes)


        val passText: TextView= findViewById(R.id.textPwdRes)

        val email = correoText.text.toString()
        val password = passText.text.toString()



        if (esCorreoValido(email)) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        enviarCorreoVerificacion()





                        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)


                        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
                        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)

                        val selectedGender = selectedRadioButton.text.toString()


                        Log.d("selectgen", "El genero es $selectedGender")

                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            val databaseRef = FirebaseDatabase.getInstance().reference
                            val userRef = databaseRef.child("users").child(userId).child("info")

                            val name = textNombre.text.toString()
                            userRef.child("name").setValue(name)


                            val career = textCar.text.toString()
                            userRef.child("career").setValue(career)

                            userRef.child("gender").setValue(selectedGender)

                        }







































                        val intent = Intent(this, Login::class.java)
                    startActivity(intent)



                    }
//                    else {
//                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                    }
                }




        } else {
            Log.d("Registro", "Correo no válido: $email")
            Toast.makeText(this, "El correo electrónico no pertenece a la UBB", Toast.LENGTH_SHORT).show()
        }






    }



    private fun esCorreoValido(email: String): Boolean {
        val dominiosPermitidos = listOf("alumnos.ubiobio.cl","ubiobio.cl", "gmail.com")

        for (dominio in dominiosPermitidos) {
            if (email.endsWith(dominio)) {
                return true
            }
        }

        return false
    }


    private fun enviarCorreoVerificacion() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se ha enviado un correo de verificación", Toast.LENGTH_SHORT).show()

                } else {

                    Toast.makeText(this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show()
                }
            }
    }








}