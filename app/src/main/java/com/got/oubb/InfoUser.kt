package com.got.oubb

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class InfoUser : AppCompatActivity() {


    private lateinit var nombreTextSet: EditText
    private lateinit var carreraTextSet: EditText
    private lateinit var vehiculoset: EditText
    private lateinit var vehiculosetPatente: EditText
    private lateinit var btnVehiculo: Button
    private lateinit var vehiculoBtnPatente: Button
    private lateinit var btnSave: Button
    private lateinit var btnCan: Button
    private lateinit var nombreBtn: Button
    private lateinit var carreraBtn: Button
    private lateinit var nombreTextView: TextView
    private lateinit var carreraTextView: TextView
    private lateinit var vehiculoText: TextView
    private lateinit var vehiculoTextPatente: TextView



    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var radioGroup: RadioGroup

    private lateinit var vehiculoView: View


    private lateinit var vehiculoViewPatante: View





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_user)




        nombreTextView = findViewById(R.id.nombre)
        carreraTextView = findViewById(R.id.carrera)
        nombreTextSet = findViewById(R.id.nombreset)
        carreraTextSet = findViewById(R.id.carreraset)

        vehiculoText = findViewById(R.id.vehiculoText)
        vehiculoTextPatente = findViewById(R.id.vehiculoTextPatente)


        vehiculoset=  findViewById(R.id.vehiculoset)
        vehiculosetPatente = findViewById(R.id.vehiculosetPatente)

        btnVehiculo = findViewById(R.id.vehiculoBtn)
        vehiculoBtnPatente = findViewById(R.id.vehiculoBtnPatente)
        vehiculoView = findViewById(R.id.vehiculoView)
        vehiculoViewPatante = findViewById(R.id.vehiculoViewPatante)



//        nombreTextView.filters = arrayOf(object : InputFilter.LengthFilter(15) {
//            override fun filter(
//                source: CharSequence,
//                start: Int,
//                end: Int,
//                dest: Spanned,
//                dstart: Int,
//                dend: Int
//            ): CharSequence? {
//                val text = dest.toString() + source.toString()
//                if (text.length > 15) {
//                    return text.substring(0, 15) + "..."
//                }
//                return null
//            }
//        })
//
//        vehiculoText.filters = arrayOf(object : InputFilter.LengthFilter(15) {
//            override fun filter(
//                source: CharSequence,
//                start: Int,
//                end: Int,
//                dest: Spanned,
//                dstart: Int,
//                dend: Int
//            ): CharSequence? {
//                val text = dest.toString() + source.toString()
//                if (text.length > 15) {
//                    return text.substring(0, 15) + "..."
//                }
//                return null
//            }
//        })



        btnSave = findViewById(R.id.guardarInf)
        btnCan= findViewById(R.id.Cancelar)

        nombreBtn = findViewById(R.id.nombreBtn)
        carreraBtn = findViewById(R.id.carreraBtn)
        btnVehiculo= findViewById(R.id.vehiculoBtn)
        vehiculoBtnPatente = findViewById(R.id.vehiculoBtnPatente)


        radioGroup = findViewById(R.id.radioGroupGenderInfo)





        val maxLengthNombre = 28
        nombreTextSet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthNombre) {
                    nombreTextSet.setText(s.subSequence(0, maxLengthNombre))
                    nombreTextSet.setSelection(maxLengthNombre)
                }
            }
        })


        val maxLengthCarrera = 5
        carreraTextSet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthCarrera) {
                    carreraTextSet.setText(s.subSequence(0, maxLengthCarrera))
                    carreraTextSet.setSelection(maxLengthCarrera)
                }
            }
        })



        val maxLengthInfovehiculo = 26
        vehiculoset.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthInfovehiculo) {
                    vehiculoset.setText(s.subSequence(0, maxLengthInfovehiculo))
                    vehiculoset.setSelection(maxLengthInfovehiculo)
                }
            }
        })

        val maxLengthInfopatente = 8
        vehiculosetPatente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLengthInfopatente) {
                    vehiculosetPatente.setText(s.subSequence(0, maxLengthInfopatente))
                    vehiculosetPatente.setSelection(maxLengthInfopatente)
                }
            }
        })







        cargarInfoUsuario()


        val userId = firebaseAuth.currentUser?.uid





        btnSave.setOnClickListener {


            if (userId != null) {
                val databaseRef = FirebaseDatabase.getInstance().reference
                val userRef = databaseRef.child("users").child(userId)
                userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(roleSnapshot: DataSnapshot) {
                        if (roleSnapshot.exists()) {
                            val role = roleSnapshot.getValue(String::class.java)
                            if (role == "oferente") {
                                val infoRef = userRef.child("info")
                                infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(infoSnapshot: DataSnapshot) {
                                        if (infoSnapshot.exists()) {
                                            val vehicle = infoSnapshot.child("vehicle").getValue(String::class.java)
                                            val patent = infoSnapshot.child("patent").getValue(String::class.java)

                                            if (vehicle.isNullOrEmpty() && patent.isNullOrEmpty()) {
                                                if (vehiculoset.text.isBlank() || vehiculosetPatente.text.isBlank()) {
                                                    Toast.makeText(applicationContext, "Rellene los campos de vehículo y patente", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    saveUserInfo()
                                                }

                                            } else{

                                                saveUserInfo()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            } else {
                                saveUserInfo()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }









        }


        btnCan.setOnClickListener {
            if (userId != null) {
                val databaseRef = FirebaseDatabase.getInstance().reference
                val userRef = databaseRef.child("users").child(userId)
                userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(roleSnapshot: DataSnapshot) {
                        if (roleSnapshot.exists()) {
                            val role = roleSnapshot.getValue(String::class.java)
                            if (role == "oferente") {
                                val infoRef = userRef.child("info")
                                infoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(infoSnapshot: DataSnapshot) {
                                        if (infoSnapshot.exists()) {
                                            val vehicle = infoSnapshot.child("vehicle").getValue(String::class.java)
                                            val patent = infoSnapshot.child("patent").getValue(String::class.java)

                                            if (!vehicle.isNullOrEmpty() && !patent.isNullOrEmpty()) {
                                                val i = Intent(this@InfoUser, MainActivity::class.java)
                                                startActivity(i)
                                                finish()
                                            }
                                            else {
                                                 Toast.makeText(applicationContext, "Rellene y guarde los campos de vehículo y patente", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            } else {
                                val intentC = Intent(this@InfoUser, MainActivity::class.java)
                                startActivity(intentC)
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar error
                    }
                })
            }
        }





        nombreBtn.setOnClickListener {
            cambiarAEditText(nombreTextView, nombreTextSet)
        }

        carreraBtn.setOnClickListener {
            cambiarAEditText(carreraTextView, carreraTextSet)
        }
        btnVehiculo.setOnClickListener {
            cambiarAEditText(vehiculoText,vehiculoset)
        }
        vehiculoBtnPatente.setOnClickListener {
            cambiarAEditText(vehiculoTextPatente,vehiculosetPatente)
        }



    }






    private fun saveUserInfo() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            val userRef = databaseRef.child("users").child(userId).child("info")

            if (nombreTextSet.visibility == View.VISIBLE) {
                val name = nombreTextSet.text.toString()
                userRef.child("name").setValue(name)
            }

            if (carreraTextSet.visibility == View.VISIBLE) {
                val career = carreraTextSet.text.toString()
                userRef.child("career").setValue(career)
            }

            if (vehiculoset.visibility == View.VISIBLE) {
                val vehicle = vehiculoset.text.toString()
                userRef.child("vehicle").setValue(vehicle)
            }

            if (vehiculosetPatente.visibility == View.VISIBLE) {
                val vehicleP = vehiculosetPatente.text.toString()
                userRef.child("patent").setValue(vehicleP)
            }




            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val selectedGender = selectedRadioButton.text.toString()
            userRef.child("gender").setValue(selectedGender)




            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }





    private fun cargarInfoUsuario() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            val userRef = databaseRef.child("users").child(userId)
            userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(roleSnapshot: DataSnapshot) {
                    if (roleSnapshot.exists()) {
                        val role = roleSnapshot.getValue(String::class.java)
                        if(role=="oferente"){

                            vehiculoText.visibility=TextView.VISIBLE
                            vehiculoTextPatente.visibility=TextView.VISIBLE
                            btnVehiculo.visibility= Button.VISIBLE
                            vehiculoBtnPatente.visibility= Button.VISIBLE
                            vehiculoView.visibility = View.VISIBLE
                            vehiculoViewPatante.visibility = View.VISIBLE

                        }
                    }
                    val infoRef = userRef.child("info")
                    infoRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val nombre = snapshot.child("name").getValue(String::class.java)?: ""
                                val carrera = snapshot.child("career").getValue(String::class.java)?: ""
                                val genero = snapshot.child("gender").getValue(String::class.java)?: ""
                                val vehiculo = snapshot.child("vehicle").getValue(String::class.java)?: ""
                                val vehiculoP = snapshot.child("patent").getValue(String::class.java)?: ""


                                //nombreTextView.text = nombre
                                nombreTextView.text = if (nombre.isNotEmpty()) nombre else "Nombre"
                                carreraTextView.text = if (carrera.isNotEmpty()) carrera else "Carrera"
                                vehiculoText.text = if (vehiculo.isNotEmpty()) vehiculo else "Modelo y color"
                                vehiculoTextPatente.text = if (vehiculoP.isNotEmpty()) vehiculoP else "Patente"


                                for (i in 0 until radioGroup.childCount) {
                                    val radioButton = radioGroup.getChildAt(i) as RadioButton
                                    if (radioButton.text.toString() == genero) {
                                        radioButton.isChecked = true
                                        break
                                    }
                                }


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })


                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
        }



    }


    private fun cambiarAEditText(textView: TextView, editText: EditText) {
        editText.text = Editable.Factory.getInstance().newEditable(textView.text)
        editText.visibility = View.VISIBLE
        textView.visibility = View.GONE
    }


}
