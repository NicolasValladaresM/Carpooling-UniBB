package com.got.oubb

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.got.oubb.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject


interface OnCalificarButtonEnabledListener {
    fun onCalificarButtonEnabled(enabled: Boolean)
}

class ConfirmFragment : DialogFragment() {
    private lateinit var databaseRef: DatabaseReference
    private lateinit var requestQueue: RequestQueue
    private lateinit var userId: String
    private lateinit var ReUserUserId: String



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_confirm, container, false)


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

            val btnConf = view.findViewById<Button>(R.id.Conf)
          //  val textId= view.findViewById<TextView>(R.id.textvieUsuario)

            if (::ReUserUserId.isInitialized) {



//                    val message = "El Usuario ha aceptado el traslado"
//                    val targetUserId = ReUserUserId
//
//                   // sendMessageToUser(targetUserId, message)
//                    unsubscribeFromCustomTopic(targetUserId)
//
//
//                    val notification = hashMapOf<Any?, Any?>(
//                        "title" to "Decidir",
//                        "body" to message,
//                        "targetUserId" to targetUserId
//                    )
//
//                    val jsonPayload = hashMapOf<Any?, Any?>(
//                        "notification" to notification,
//                        "to" to "/topics/user_$targetUserId"
//                    )
//
//                    val url = "https://fcm.googleapis.com/fcm/send"
//                    val request = object :
//                        JsonObjectRequest(
//                            Method.POST,
//                            url,
//                            JSONObject(jsonPayload as Map<Any?, Any?>),
//                            {
//                                //  Toast.makeText(context, "mensaje enviado", Toast.LENGTH_SHORT).show()
//
//                            },{}) {
//                        override fun getHeaders(): MutableMap<String, String> {
//                            val headers = HashMap<String, String>()
//                            headers["Authorization"] = "Bearer AAAA9_h7TfY:APA91bH1bJtcm62vZ9x_jmQIbjD3JtfQVSGnWnqFPra0uxsX6jX7gR-4wKZUczVABuIG9z5Hu8VvMzB_7i7kTG6NXFlW42n1-2xn7lGzg0Y0sQd1upDNrLCLaodwoufiGKnYG6Y3VbRh"
//                            headers["Content-Type"] = "application/json"
//                            return headers
//                        }
//                    }
//
//                    requestQueue.add(request)


                btnConf.setOnClickListener {

                    dismiss()

                }

            }


        }



    }



    private fun unsubscribeFromCustomTopic(userId: String?) {
        if (userId != null) {
            val topic = "user_$userId"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }

    fun setReUser(userId: String){
        this.ReUserUserId=userId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }


}