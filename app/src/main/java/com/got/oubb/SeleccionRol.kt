package com.got.oubb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SeleccionRol : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_rol)


        firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid

        val btnSoli = findViewById<Button>(R.id.solicitante)
        val btnOfer = findViewById<Button>(R.id.oferente)

        val user = firebaseAuth.currentUser

        if (user?.isEmailVerified == false){

                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            finish()

        }

        val intent = Intent(this, MainActivity::class.java)

        btnSoli.setOnClickListener {




            if (userId != null) {
                val usersRef = FirebaseDatabase.getInstance().reference.child("users")

                val userNodeRef = usersRef.child(userId)

                userNodeRef.child("role").setValue("solicitante")
            }


            startActivity(intent)
            finish()


        }

        btnOfer.setOnClickListener {




            if (userId != null) {
                val usersRef = FirebaseDatabase.getInstance().reference.child("users")

                val userNodeRef = usersRef.child(userId)

                userNodeRef.child("role").setValue("oferente")

                noVehicle()


            }


        }
    }






    private var redirectionDone = false

    private fun noVehicle() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            val userRef = databaseRef.child("users").child(userId)
            userRef.child("role").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(roleSnapshot: DataSnapshot) {
                    if (roleSnapshot.exists()) {
                        val role = roleSnapshot.getValue(String::class.java)
                        if (role == "oferente") {
                            val infoRef = userRef.child("info")
                            val infoListener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val vehicle = snapshot.child("vehicle").getValue(String::class.java)
                                        val patent = snapshot.child("patent").getValue(String::class.java)

                                        if (vehicle.isNullOrEmpty() || patent.isNullOrEmpty()) {
                                            Toast.makeText(applicationContext, "Rellene los datos faltantes", Toast.LENGTH_SHORT).show()
                                            val i = Intent(applicationContext, InfoUser::class.java)
                                            startActivity(i)
                                            finish()
                                        }
                                        else{

                                            val i = Intent(applicationContext, MainActivity::class.java)
                                            startActivity(i)
                                            finish()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            }
                            infoRef.addListenerForSingleValueEvent(infoListener)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }




}
