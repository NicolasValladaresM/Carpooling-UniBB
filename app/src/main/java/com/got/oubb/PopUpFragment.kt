package com.got.oubb


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject





class PopUpFragment : DialogFragment(),OnCalificarButtonEnabledListener {



    private var activity: Activity? = null


    private lateinit var targetUserIdDecide: String
    private lateinit var requestQueue: RequestQueue
    private lateinit var userId: String


    private lateinit var databaseRef: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_pop_up, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            this.activity = context
        }
    }

    private fun sendMessageToUser(targetUserId: String, message: String) {

        val databaseRef = FirebaseDatabase.getInstance().reference
        val messageObject = HashMap<String, Any>()
        messageObject["fromUserId"] = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        messageObject["message"] = message



        databaseRef.child("users").child(targetUserId)
            .child("messages")
            .push()
            .setValue(messageObject)

    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Assuming you have a reference to your activity


        val context = requireContext()

        databaseRef = FirebaseDatabase.getInstance().reference


        requestQueue = Volley.newRequestQueue(context)

        val btnSoli= view.findViewById<Button>(R.id.btn_boton1)
        val btnCan=view.findViewById<Button>(R.id.btn_boton2)

        subscribeToCustomTopic(userId)

        btnSoli.setOnClickListener {


            val message = "Traslado solicitado"
            val targetUserId = targetUserIdDecide

            if (targetUserId.isNotEmpty()) {

                sendMessageToUser(targetUserId, message)


                val decideFragment = DecideFragment()
                decideFragment.setReUser(userId)
                decideFragment.setUserId(targetUserIdDecide)


                val openDecideMessage = HashMap<String, Any>();
                openDecideMessage["type"] = "open_decide";
                openDecideMessage["fromUserId"] = FirebaseAuth.getInstance().currentUser?.uid!!

                databaseRef.child("users").child(this.targetUserIdDecide).child("messages").push().setValue(openDecideMessage)


                    val notification = hashMapOf<Any?, Any?>(
                        "title" to "Traslado",
                        "body" to message,
                        "targetUserId" to targetUserId
                    )


                    val jsonPayload = hashMapOf<Any?, Any?>(
                        "notification" to notification,
                        "to" to "/topics/user_$targetUserId"
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
                            headers["Authorization"] = "Bearer AAAA_BLol2Q:APA91bGGeL2eAAium-eZgsvA0LKDGHxB_atWT2kM7i4NJAh81edSdEEbeD_gLuj8q85m6yFqlXPk_lgPf2G0e__ZBnfdcS6k-XxOau9Be8XDZIUCvXRK9FJxaN7KN0e1quXAVhsIB_pM"
                            headers["Content-Type"] = "application/json"
                            return headers
                        }
                    }
                    requestQueue.add(request)


                    databaseRef.child("users").child(this.targetUserIdDecide).child("messages").removeValue()
                    databaseRef.child("users").child(this.userId).child("messages").removeValue()





                unsubscribeFromCustomTopic(targetUserId)

                dismiss()


            }



        }

        btnCan.setOnClickListener {


            val firebaseAuth = FirebaseAuth.getInstance()
            val userId = firebaseAuth.currentUser?.uid

            if (userId != null) {
                val databaseRef = FirebaseDatabase.getInstance().reference
                val userRef = databaseRef.child("users").child(userId)
                userRef.child("evaluating").setValue(false)

            }


            Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
            dismiss()

    }









        //val txtvie = view.findViewById<TextView>(R.id.textviewid)
        val txtName = view.findViewById<TextView>(R.id.name)
        val txtCareer = view.findViewById<TextView>(R.id.carrer)
        val txtGender = view.findViewById<TextView>(R.id.gender)
        val txtnumAsientos = view.findViewById<TextView>(R.id.selectNumber)

        val txtMmalo = view.findViewById<TextView>(R.id.Mmalo)
        val txtMalo = view.findViewById<TextView>(R.id.Malo)
        val txtregular = view.findViewById<TextView>(R.id.Regular)
        val txtbueno = view.findViewById<TextView>(R.id.Bueno)
        val txtMbueno=view.findViewById<TextView>(R.id.Mbueno)

        val vehiculoText = view.findViewById<TextView>(R.id.infoVehiculoPop)
        val vehiculoTextPatente = view.findViewById<TextView>(R.id.infoVehiculoPat)


        val textinfo = view.findViewById<TextView>(R.id.info)
        textinfo.text = "Información de usuario"
        textinfo.paintFlags = textinfo.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val textrepu = view.findViewById<TextView>(R.id.inforepu)
        textrepu.text = "Puntos de Reputación"
        textrepu.paintFlags = textrepu.paintFlags or Paint.UNDERLINE_TEXT_FLAG



        databaseRef.child("users").child(targetUserIdDecide).addListenerForSingleValueEvent(object :
            ValueEventListener {
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

                    val seaticonView = view.findViewById<View>(R.id.seaticon)
                    seaticonView.visibility = View.VISIBLE


                    txtnumAsientos.text= "Asientos Disponibles: ${numAsientos?: 0} "

                    vehiculoText.text="Vehiculo: ${vehicle}"
                    vehiculoTextPatente.text= "Patente: ${patent}"



                    val autoView = view.findViewById<View>(R.id.infoVehiculoView)
                    autoView.visibility = View.VISIBLE

                    val patenteView = view.findViewById<View>(R.id.infoVehiculoViewPat)
                    patenteView.visibility = View.VISIBLE






                }



                val totalCalificaciones = muymalo + malo + regular + bueno + muybueno
                val promedioPonderado = (muymalo * 1 + malo * 2 + regular * 3 + bueno * 4 + muybueno * 5).toDouble() / totalCalificaciones


                val iconprome = view.findViewById<View>(R.id.iconprome)

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

    override fun onCalificarButtonEnabled(enabled: Boolean) {
        val activity = activity
        if (activity is OnCalificarButtonEnabledListener) {
            activity.onCalificarButtonEnabled(enabled)
        }
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }


    fun targetUserIdDecide(userId: String) {
        targetUserIdDecide = userId
    }




    private fun subscribeToCustomTopic(userId: String) {
        val topic = "user_$userId"
        FirebaseMessaging.getInstance().subscribeToTopic(topic)

    }


    private fun unsubscribeFromCustomTopic(userId: String?) {
        if (userId != null) {
            val topic = "user_$userId"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
    }

}





