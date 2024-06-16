package com.got.oubb

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

interface OnReputationActionListener {
    fun onCalificarButtonEnabled(enabled: Boolean)
}


class RepuFragment : DialogFragment() {

    private lateinit var calificarButton: Button
    private var currentUserId: String = ""
    private  var otherUserId: String=""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_repu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val btnBueno = view.findViewById<Button>(R.id.btnBueno)
        val btnMalo = view.findViewById<Button>(R.id.btnMalo)
        val btnSin = view.findViewById<Button>(R.id.btnSin)

        val btnBuenoM = view.findViewById<Button>(R.id.btnBuenoM)
        val btnMaloM = view.findViewById<Button>(R.id.btnMaloM)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val evaluatingRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId!!)
            .child("evaluating")




        btnBueno.setOnClickListener {

            val type = "Bueno"
            increaseReputation(type)
            calificarButton.isEnabled = false
            Log.d("Valores", "Los valores de variables actuales son  $otherUserId y $currentUserId")



            evaluatingRef.setValue(false)


            dismiss()
        }

        btnBuenoM.setOnClickListener {

            val type = "Muy bueno"
            increaseReputation(type)
            calificarButton.isEnabled = false

            evaluatingRef.setValue(false)


            dismiss()
        }




        btnMalo.setOnClickListener {
            val type = "Malo"
            increaseReputation(type)
            calificarButton.isEnabled = false

            Log.d("Valores", "Los valores de variables actuales son  $otherUserId y $currentUserId")

            evaluatingRef.setValue(false)




            dismiss()
        }

        btnMaloM.setOnClickListener {
            val type = "Muy malo"
            increaseReputation(type)
            calificarButton.isEnabled = false


            evaluatingRef.setValue(false)




            dismiss()
        }

        btnSin.setOnClickListener {
            val type = "Regular"
            increaseReputation(type)
            calificarButton.isEnabled = false

            Log.d("Valores", "Los valores de variables actuales son  $otherUserId y $currentUserId")

            evaluatingRef.setValue(false)

            dismiss()
        }
    }




    private fun increaseReputation(type: String) {

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()


            if (currentUser == currentUserId) {
                // El usuario actual es igual a currentUserId

                val otherUserReputationRef = database.getReference("users").child(otherUserId).child("reputation")
                otherUserReputationRef.child(type).setValue(ServerValue.increment(1))
                Log.d("BtnBuenoPresionado", "El valor fue asignado como sub nodo al nodo $otherUserId")

            }
            else if (currentUser == otherUserId) {
                // El usuario actual es igual a otherUserId
                val currentUserReputationRef = database.getReference("users").child(currentUserId).child("reputation")
                currentUserReputationRef.child(type).setValue(ServerValue.increment(1))
                Log.d("BtnBuenoPresionado", "El valor al nodo $currentUserId  llamado de esta forma")


            }


    }






    fun setCalificarButton(button: Button) {
        calificarButton = button
    }
    fun setUserIds(userId: String, otherUserId: String) {
        this.currentUserId = userId
        this.otherUserId = otherUserId
    }





}

