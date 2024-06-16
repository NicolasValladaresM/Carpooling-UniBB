package com.example.prueba


import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject





class PopUpFragment : DialogFragment(),OnCalificarButtonEnabledListener {



    private lateinit var requestQueue: RequestQueue
    private lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_pop_up, container, false)
    }

    private fun sendMessageToUser(targetUserId: String, message: String) {

        val databaseRef = FirebaseDatabase.getInstance().reference
        val messageObject = HashMap<String, Any>()
        messageObject["fromUserId"] = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        messageObject["message"] = message


        databaseRef.child("users").child(targetUserId).child("messages").push().setValue(messageObject)
            .addOnSuccessListener {
                Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
                dismiss()
            }

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       val context = requireContext()
        requestQueue = Volley.newRequestQueue(context)

        val btnSoli= view.findViewById<Button>(R.id.btn_boton1)
        val btnCan=view.findViewById<Button>(R.id.btn_boton2)


        val txtvie = view.findViewById<TextView>(R.id.textviewid)

        txtvie.text= userId




        btnSoli.setOnClickListener {
            val message = "mensaje de prueba" //
            val targetUserId = userId //



            if (targetUserId.isNotEmpty()) {
                //subscribeToCustomTopic(targetUserId)
                sendMessageToUser(targetUserId, message)


                Log.d("Valor del target id desde el Popup",targetUserId)






            } else {
                Toast.makeText(context, "No se ha establecido el userId", Toast.LENGTH_SHORT).show()
            }




            val notification = hashMapOf<Any?, Any?>(
                "title" to "Nuevo mensaje",
                "body" to message,
                "targetUserId" to targetUserId
            )


            val jsonPayload = hashMapOf<Any?, Any?>(
                "notification" to notification,
                "to" to "/topics/user_$targetUserId" //topic personalizado para el usuario destino
            )

            val url = "https://fcm.googleapis.com/fcm/send"
            val request = object : JsonObjectRequest(Method.POST, url, JSONObject(jsonPayload as Map<Any?, Any?>),
                {
                    Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show()

                },
                {
                    Toast.makeText(context, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer clave de servidor de FCM
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }


            requestQueue.add(request)



        }



        btnCan.setOnClickListener {
            Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
            dismiss()
        }




    }

    override fun onCalificarButtonEnabled(enabled: Boolean) {
        val activity = activity
        if (activity is OnCalificarButtonEnabledListener) {
            activity.onCalificarButtonEnabled(enabled)
        }
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }







}





