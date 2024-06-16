package com.got.oubb

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import androidx.fragment.app.DialogFragment
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging



class DecideFragment : DialogFragment()  {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var requestQueue: RequestQueue
    private lateinit var userId: String
    private lateinit var ReUserUserId: String



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_decide, container, false)


    }

    private fun sendMessageToUser(targetUserId: String, message: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val messageObject = HashMap<String, Any>()
        messageObject["fromUserId"] = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        messageObject["message"] = message
        messageObject["accepted"] = false

        val openDecideMessage = HashMap<String, Any>()
        openDecideMessage["type"] = "open_decide_Resp"
        openDecideMessage["fromUserId"] = FirebaseAuth.getInstance().currentUser?.uid!!

        databaseRef.child("users").child(targetUserId)
            .child("messages")
            .push()
            .setValue(openDecideMessage)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if(::userId.isInitialized && ::ReUserUserId.isInitialized){

            setUserId(userId)
            setReUser(ReUserUserId)


            databaseRef = FirebaseDatabase.getInstance().reference




            val context = requireContext()
            requestQueue = Volley.newRequestQueue(context)

            val btnAcept = view.findViewById<Button>(R.id.btn_boton1D)
            val btnRech =  view.findViewById<Button>(R.id.btn_boton2D)

            if (::ReUserUserId.isInitialized) {

                subscribeToCustomTopic(userId)

                btnAcept.setOnClickListener {


                        val message = "El usuario ha aceptado el traslado"
                        val targetUserId = ReUserUserId  //ID del usuario destino

                        sendMessageToUser(targetUserId, message)

                        val notification = hashMapOf<Any?, Any?>(
                            "title" to "Resultado de solicitud",
                            "body" to message,
                            "targetUserId" to targetUserId
                        )

                        val jsonPayload = hashMapOf<Any?, Any?>(
                            "notification" to notification,
                            "to" to "/topics/user_$targetUserId" //topic personalizado para el usuario destino
                        )

                        val url = "https://fcm.googleapis.com/fcm/send"
                        val request = object :
                            JsonObjectRequest(Method.POST,
                                url,
                                JSONObject(jsonPayload as Map<Any?, Any?>),
                                {
                                    Toast.makeText(context, "mensaje enviado", Toast.LENGTH_SHORT).show()

                                },
                                {
                                    Toast.makeText(context, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
                                }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Authorization"] = "Bearer AAAA_BLol2Q:APA91bGGeL2eAAium-eZgsvA0LKDGHxB_atWT2kM7i4NJAh81edSdEEbeD_gLuj8q85m6yFqlXPk_lgPf2G0e__ZBnfdcS6k-XxOau9Be8XDZIUCvXRK9FJxaN7KN0e1quXAVhsIB_pM"
                                headers["Content-Type"] = "application/json"
                                return headers
                            }
                        }



                    requestQueue.add(request)




                    val userRef = databaseRef.child("users").child(ReUserUserId)
                    databaseRef.child("users").child(ReUserUserId).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var asiento= dataSnapshot.child("selectedNumber").getValue(Int::class.java)
                            if (asiento != null && asiento > 0) {
                                asiento=asiento-1
                                userRef.child("selectedNumber").setValue(asiento)

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })

                    databaseRef.child("users").child(this.ReUserUserId).child("messages").removeValue()
                    databaseRef.child("users").child(this.userId).child("messages").removeValue()


                    unsubscribeFromCustomTopic(ReUserUserId)
                    dismiss()



                }

            }
            btnRech.setOnClickListener {
                Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
                val firebaseAuth = FirebaseAuth.getInstance()
                val userId = firebaseAuth.currentUser?.uid
                userId?.let {
                val usersRef = FirebaseDatabase.getInstance().reference.child("users")
                val evaluatingRef = usersRef.child(it).child("evaluating")

                    evaluatingRef.setValue(false)
            }
                dismiss()
            }





            val txtName = view.findViewById<TextView>(R.id.nameD)
            val txtCareer = view.findViewById<TextView>(R.id.carrerD)
            val txtGender = view.findViewById<TextView>(R.id.genderD)
            val txtnumAsientos = view.findViewById<TextView>(R.id.selectNumberD)

            val txtMmalo = view.findViewById<TextView>(R.id.MmaloD)
            val txtMalo = view.findViewById<TextView>(R.id.MaloD)
            val txtregular = view.findViewById<TextView>(R.id.RegularD)
            val txtbueno = view.findViewById<TextView>(R.id.BuenoD)
            val txtMbueno=view.findViewById<TextView>(R.id.MbuenoD)

            val vehiculoText = view.findViewById<TextView>(R.id.infoVehiculoPopD)
            val vehiculoTextPatente = view.findViewById<TextView>(R.id.infoVehiculoPatD)


            val textinfo = view.findViewById<TextView>(R.id.infoD)
            textinfo.text = "Puntos de Reputación"
            textinfo.paintFlags = textinfo.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            val textrepu = view.findViewById<TextView>(R.id.inforepuD)
            textrepu.text = "Puntos de Reputación"
            textrepu.paintFlags = textrepu.paintFlags or Paint.UNDERLINE_TEXT_FLAG


            databaseRef.child("users").child(ReUserUserId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val career = dataSnapshot.child("info").child("career").getValue(String::class.java)
                    val gender = dataSnapshot.child("info").child("gender").getValue(String::class.java)
                    val name = dataSnapshot.child("info").child("name").getValue(String::class.java)
                    val role = dataSnapshot.child("role").getValue(String::class.java)
                    val muymalo = dataSnapshot.child("reputation").child("Muy malo").getValue(Int::class.java) ?: 0
                    val malo = dataSnapshot.child("reputation").child("Malo").getValue(Int::class.java) ?: 0
                    val regular = dataSnapshot.child("reputation").child("Regular").getValue(Int::class.java) ?: 0
                    val bueno = dataSnapshot.child("reputation").child("Bueno").getValue(Int::class.java) ?: 0
                    val muybueno = dataSnapshot.child("reputation").child("Muy bueno").getValue(Int::class.java) ?: 0

                    if(role=="oferente"){

                        val numAsientos = dataSnapshot.child("selectedNumber").getValue(Int::class.java)
                        val vehicle = dataSnapshot.child("info").child("vehicle").getValue(String::class.java)
                        val patent = dataSnapshot.child("info").child("patent").getValue(String::class.java)

                        val seaticonView = view.findViewById<View>(R.id.seaticonD)
                        seaticonView.visibility = View.VISIBLE

                        txtnumAsientos.text= "Asientos Disponibles: ${numAsientos?: 0} "

                        vehiculoText.text="Vehiculo: ${vehicle}"
                        vehiculoTextPatente.text= "Patente: ${patent}"


                        val autoView = view.findViewById<View>(R.id.infoVehiculoViewD)
                        autoView.visibility = View.VISIBLE

                        val patenteView = view.findViewById<View>(R.id.infoVehiculoViewPatD)
                        patenteView.visibility = View.VISIBLE

                    }


                    val totalCalificaciones = muymalo + malo + regular + bueno + muybueno
                    val promedioPonderado = (muymalo * 1 + malo * 2 + regular * 3 + bueno * 4 + muybueno * 5).toDouble() / totalCalificaciones


                    val iconprome = view.findViewById<View>(R.id.iconpromeD)

                    when {
                        promedioPonderado >= 1.0 && promedioPonderado < 2.0 -> iconprome.setBackgroundResource(R.mipmap.muymalo)
                        promedioPonderado >= 2.0 && promedioPonderado < 3.0 -> iconprome.setBackgroundResource(R.mipmap.malo)
                        promedioPonderado >= 3.0 && promedioPonderado < 4.0 -> iconprome.setBackgroundResource(R.mipmap.regular)
                        promedioPonderado >= 4.0 && promedioPonderado < 5.0 -> iconprome.setBackgroundResource(R.mipmap.bueno)
                        promedioPonderado >= 5.0 -> iconprome.setBackgroundResource(R.mipmap.muybueno)
                        else -> {
                            // Manejar otros casos o proporcionar un fondo predeterminado
                            iconprome.setBackgroundResource(R.mipmap.defaultcali)
                        }
                    }

                    txtName.text = "Nombre: $name"
                    txtCareer.text = "Carrera: $career"
                    txtGender.text = "Genero: $gender"
                    txtMmalo.text = "Muy Malo: ${muymalo ?: 0}"
                    txtMalo.text = "Malo: ${malo ?: 0}"
                    txtregular.text = "Regular: ${regular ?: 0}"
                    txtbueno.text = "Bueno: ${bueno ?: 0}"
                    txtMbueno.text = "Muy Bueno: ${muybueno ?: 0}"

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })








        }



    }

    private fun unsubscribeFromCustomTopic(userId: String?) {
        if (userId != null) {
            val topic = "user_$userId"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }

    private fun subscribeToCustomTopic(userId: String) {
        val topic = "user_$userId"
        FirebaseMessaging.getInstance().subscribeToTopic(topic)

    }

fun setReUser(userId: String){
    this.ReUserUserId=userId
}

fun setUserId(userId: String) {
    this.userId = userId


}



}