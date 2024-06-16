package com.got.oubb

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import java.util.HashMap

interface OnDestinationChangedListener {
    fun onDestinationChanged(latitude: Double, longitude: Double)
}

class OriginCoords : DialogFragment() {
    var listener: OnDestinationChangedListener? = null




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_origin_coords, container, false)
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val SaveUbi=view.findViewById<Button>(R.id.saveUbi)


        SaveUbi.setOnClickListener {
            Log.d("Presionado", "El botonsaveubifuepresionado")

            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    saveUserLocationToFirebase(location.latitude, location.longitude)
                }
            }

            dismiss()
        }




        val ubiSaved=view.findViewById<Button>(R.id.ubiSaved)


        ubiSaved.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let {
                val usersRef = FirebaseDatabase.getInstance().reference.child("users")
                val userLocationRef = usersRef.child(userId).child("locationSaved")
                userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val location = dataSnapshot.value as? HashMap<*, *>
                        val latitude = location?.get("latitude") as? Double
                        val longitude = location?.get("longitude") as? Double

                        if (latitude != null && longitude != null) {
//                            destinationLatitude = latitude
//                            destinationLongitude = longitude

                            listener?.onDestinationChanged(latitude, longitude)

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar errores de lectura de la base de datos aquí si es necesario
                    }
                })
            }

            dismiss()
        }

        val haciaUbb=view.findViewById<Button>(R.id.haciaUbb)

        haciaUbb.setOnClickListener {

            listener?.onDestinationChanged(-36.822675, -73.013227)
            dismiss()

        }




        val ubiCancel=view.findViewById<Button>(R.id.cancel)

        ubiCancel.setOnClickListener{

            dismiss()
        }


    }


    private fun saveUserLocationToFirebase(latitude: Double, longitude: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val userCoordinates = HashMap<String, Any>()
            userCoordinates["latitude"] = latitude
            userCoordinates["longitude"] = longitude

            val usersRef = FirebaseDatabase.getInstance().reference.child("users")
            val userLocationRef = usersRef.child(userId).child("locationSaved")
            userLocationRef.setValue(userCoordinates)
        }



    }



}