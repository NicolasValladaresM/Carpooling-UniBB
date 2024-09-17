package com.got.oubb


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import org.json.JSONObject
import android.app.AlertDialog
import android.widget.ImageView
import android.widget.NumberPicker

import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import com.google.android.gms.maps.model.CameraPosition

import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Observer
import com.google.android.material.snackbar.Snackbar

import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback, ActivityResultRegistryOwner, NavigationView.OnNavigationItemSelectedListener,OnCalificarButtonEnabledListener,OnReputationActionListener,
    OnDestinationChangedListener,Filters.FiltroDialogListener {


    private var currentUserId: String = ""
    private  var otherUserId: String=""

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle:ActionBarDrawerToggle

    private lateinit var calificarButton: Button

    private lateinit var asientos: Button
    private var isCalificarButtonEnabled = false

    private var currentFilter: String? = null
    private lateinit var nombreTextView: TextView


    override fun onFiltroSelected(filtro: String?) {
        currentFilter = filtro


    }


    private lateinit var dialog: AlertDialog


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
        setMinUpdateDistanceMeters(0f)
        setGranularity(Granularity.GRANULARITY_FINE)
        setWaitForAccurateLocation(true)
    }.build()

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            requestLocationPermission()
        } else {
            // Handle the case where notification permission is not granted
        }
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, notificationPermission) == PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission()
            } else {
                requestNotificationPermissionLauncher.launch(notificationPermission)
            }

            if (!hasLocationPermission()) {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        //noVehicle()

        createMapFragment()


        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            subscribeToCustomTopic(userId)
        }







        energiaAndSideBar()
        enableAsientosYcerrarYcalificar()
        cargarInfoUsuario()
        listenForMessages()

       //

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
                        if(role=="oferente"){
                            val infoRef = userRef.child("info")
                            infoRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val vehicle = snapshot.child("vehicle").getValue(String::class.java)
                                        val patent = snapshot.child("patent").getValue(String::class.java)

                                        if (vehicle == null || patent==null){

                                          //  if (!redirectionDone) {
                                                Toast.makeText(applicationContext, "Rellene los datos faltantes", Toast.LENGTH_SHORT).show()

                                                val i = Intent(applicationContext, InfoUser::class.java)
                                                startActivity(i)
                                                redirectionDone = true
                                                finish()
                                          //  }

                                        }


                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })

                        }
                    }



                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
        }



    }








    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }



    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        if (isGranted) {

            requestBackgroundLocationPermission()
            map.isMyLocationEnabled = true
            requestLocationUpdates()

        } else {

          //  requestLocationPermission() // Solicitar nuevamente el permiso

        }
    }

    private fun requestBackgroundLocationPermission() {

        val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissionLauncher.launch(backgroundPermission)
        }


    }


    private fun requestLocationPermission() {
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val backgroundPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION


        if (ContextCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, backgroundPermission) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            requestLocationUpdates()
        } else {

            val rootView = findViewById<View>(android.R.id.content)
            val snackbar = Snackbar.make(rootView, "Permita el acceso todo el tiempo para continuar utilizando la aplicación aún cuando esté minimizada", Snackbar.LENGTH_LONG)
            snackbar.duration = 10000 // Duración en milisegundos (10 segundos)
            snackbar.show()

            requestLocationPermissionLauncher.launch(locationPermission)
           // requestLocationPermissionLauncher.launch(backgroundPermission)


        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario concedió los permisos, puedes realizar acciones relacionadas con la ubicación aquí
                //map.isMyLocationEnabled=true
            } else {
                // El usuario denego los permisos
            }
        }
    }


    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        createMarkerUbb()

//        map.setOnMyLocationButtonClickListener {
//            true
//        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }

        requestNotificationPermission()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestLocationPermission()
        }


    }


    private fun createMapFragment() {
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun energiaAndSideBar(){

        nombreTextView = findViewById(R.id.nav_header_text)
        firebaseAuth = FirebaseAuth.getInstance()
        isCalificarButtonEnabled = false


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GotoUBB:Activo")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        drawer=findViewById(R.id.drawer_layout)
        toggle= ActionBarDrawerToggle(this,drawer,toolbar,R.string.nativation_open,R.string.navigation_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

    }






    private fun enableAsientosYcerrarYcalificar(){
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val userReference = database.getReference("users/$userId")

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.child("role").getValue(String::class.java)
                val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java)
                val userCurrent = FirebaseAuth.getInstance().currentUser?.uid

                if (userId == userCurrent) {
                    if (role == "oferente") {
                        //asientos.isEnabled = false

                        asientos.isVisible=true
                    }
                }
                if(evaluating==true){


                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


        if (userId != null) {
            val usersRefCal = FirebaseDatabase.getInstance().reference.child("users")
            usersRefCal.child(userId).child("evaluating").setValue(false)

        }


        val cerrarSesion=findViewById<Button>(R.id.cerrarS)
        cerrarSesion.setOnClickListener{
            Log.d("Cerrar", "Boton se cerrar sesion fue presionado")

            deleteCoordinatesFromDatabase()
            FirebaseAuth.getInstance().signOut()

            val intentC = Intent(this@MainActivity, Login::class.java)

            startActivity(intentC)
            finish()
        }


        calificarButton = findViewById<Button>(R.id.calificar)
        calificarButton.isEnabled = false

        asientos = findViewById<Button>(R.id.asientos)
        asientos.setOnClickListener  {
            showNumberPickerDialog()
        }



    }
    private fun cargarInfoUsuario() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            val userRef = databaseRef.child("users").child(userId).child("info")

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nombre = snapshot.child("name").getValue(String::class.java)
                        nombreTextView.text=nombre
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoadImage", "Error loading user data: $error")
                }
            })
        }
    }
    private lateinit var firebaseAuth: FirebaseAuth
    private fun showNumberPickerDialog() {
        val numberPicker = NumberPicker(ContextThemeWrapper(this, R.style.numberpickerStyle))
        numberPicker.minValue = 0
        numberPicker.maxValue = 9


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val lastSelectedNumber = sharedPreferences.getInt("lastSelectedNumber", 0)
        numberPicker.value = lastSelectedNumber



        val pickerLayoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.number_picker_width),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        numberPicker.layoutParams = pickerLayoutParams

        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.gravity = Gravity.CENTER

        val titleTextView = TextView(this)
        titleTextView.text = "Asientos"
        titleTextView.gravity = Gravity.CENTER

        titleTextView.setTextAppearance(R.style.DialogTitleStyle)
        linearLayout.addView(titleTextView)

        val spaceViewBeforeNumberPicker = View(this)
        spaceViewBeforeNumberPicker.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.space_height)
        )

        linearLayout.addView(spaceViewBeforeNumberPicker)



        linearLayout.addView(numberPicker)

        val buttonLayout = LinearLayout(this)
        buttonLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.gravity = Gravity.CENTER
        buttonLayout.setPadding(30, 30, 30,0)



        val buttonAccept = Button(this)
        buttonAccept.text = "Aceptar"
        buttonAccept.setBackgroundResource(R.drawable.rounded_button_background)
        buttonAccept.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        buttonAccept.setOnClickListener {
            val selectedNumber = numberPicker.value


            val editor = sharedPreferences.edit()
            editor.putInt("lastSelectedNumber", selectedNumber)
            editor.apply()

            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {


                val databaseRef = FirebaseDatabase.getInstance().reference
                val userRef = databaseRef.child("users").child(userId)
                userRef.child("selectedNumber").setValue(selectedNumber)

            }
            if (::dialog.isInitialized) {
                dialog.dismiss()
            }

        }


        val spaceViewBetweenButtons = View(this)
        spaceViewBetweenButtons.layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.button_margin),
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        val buttonCancel = Button(this)
        buttonCancel.text = "Cancelar"
        buttonCancel.setBackgroundResource(R.drawable.rounded_button_backgroundred) // Usa el fondo personalizado

        buttonCancel.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        buttonCancel.setOnClickListener {
            if (::dialog.isInitialized) {
                dialog.dismiss()
            }

        }

        buttonLayout.addView(buttonAccept)
        buttonLayout.addView(spaceViewBetweenButtons)
        buttonLayout.addView(buttonCancel)

        linearLayout.addView(buttonLayout)

        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustom))
        builder.setView(linearLayout)

        builder.create().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog = builder.create()


        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialog.show()




    }
    override fun onDestinationChanged(latitude: Double, longitude: Double) {
        destinationLatitude = latitude
        destinationLongitude = longitude
    }
    private fun restoreOriginalDestination() {
        // Restaura el destino original llamando a la función drawRoute con las coordenadas originales
        drawRoute(originLatitude, originLongitude, -36.822675, -73.013227)
    }

    private fun deleteCoordinatesFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val coordinatesRef = FirebaseDatabase.getInstance().reference.child("coordinates")
            coordinatesRef.child(userId).removeValue()

            coordinatesRef.child(userId).onDisconnect().setValue(null)

            val marker = markerMap[userId]
            marker?.remove()



            Log.d("RealtimeDatabase", "Coordinates removed for user: $userId")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        deleteCoordinatesFromDatabase()
    }

//    override fun onBackPressed() {
//        deleteCoordinatesFromDatabase()
//        super.onBackPressed()
//    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        val firebaseAuth = FirebaseAuth.getInstance()
//        val userId = firebaseAuth.currentUser?.uid
//        val database = FirebaseDatabase.getInstance()
//        val userReference = database.getReference("users/$userId")
//
//        userReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java)
//
//                if(evaluating==true){
//                    item.isEnabled = false
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })


        when (item.itemId) {
            R.id.ingresarInf -> {
                val i = Intent(this, InfoUser::class.java)
                startActivity(i)
            }
            R.id.origenUbi -> {
                val fragment = OriginCoords()
                fragment.listener = this
                fragment.show(supportFragmentManager, "OriginCoords")
            }
            R.id.filtros -> {

            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    fun mostrarDialogoFiltro(view: View) {
        val filtroDialogFragment = Filters()
        filtroDialogFragment.setFiltroDialogListener(this)
        filtroDialogFragment.show(supportFragmentManager, "FiltroDialogFragment")
    }



    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)

    }





    private fun requestLocationUpdates() {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                REQUEST_LOCATION_PERMISSION
            )
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val latLng = LatLng(location.latitude, location.longitude)
                

                LocationUpdate(location)
                showMarkersFromDatabase(this@MainActivity,rutaActual)

                Log.d("ActualLocation", "La ubicacion actual es:  $latLng")

            }
        }
    }


    private var originLatitude: Double = 0.0
    private var originLongitude: Double = 0.0


    private var destinationLatitude: Double = -36.822675
    private var destinationLongitude: Double = -73.013227

    private fun LocationUpdate(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)


        val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .bearing(location.bearing)
                .tilt(50.0f) //inclinar la camara
                .zoom(20f)
                .build()


        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


        insertOrUpdateCoordinatesToDatabase(location.latitude, location.longitude)

        originLatitude = location.latitude
        originLongitude = location.longitude
        drawRoute(originLatitude, originLongitude, destinationLatitude, destinationLongitude)

    }




    private fun createMarkerUbb() {
        //solo acepta png para marker
        val coordenadas = LatLng(-36.822675, -73.013227)

        val marker: MarkerOptions = MarkerOptions().position(coordenadas).title("Universidad del Bio-Bio")
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.escudoubb))
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordenadas, 18f),
            4000, null

        )
    }
    private fun calculateDistanceToRoute(userLatLng: LatLng): Double {
        var closestDistance = Double.MAX_VALUE

        for (point in rutaActual?.points ?: emptyList()) {
            val routeLatLng = LatLng(point.latitude, point.longitude)
            val distance = SphericalUtil.computeDistanceBetween(userLatLng, routeLatLng)

            if (distance < closestDistance) {
                closestDistance = distance
            }
        }

        return closestDistance
    }

    val markerMap = HashMap<String, Marker?>()
    private fun updateCoordinates(snapshot: DataSnapshot) {
        val userId = snapshot.key
        val latitude = snapshot.child("latitud").getValue(Double::class.java)
        val longitude = snapshot.child("longitud").getValue(Double::class.java)

        if (userId != null && latitude != null && longitude != null) {
            val latLng = LatLng(latitude, longitude)
            val distance = calculateDistanceToRoute(latLng)

            if (distance <= 70.0  || userId == decidingUserId)  {

                if (markerMap.containsKey(userId)) {
                    val existingMarker = markerMap[userId]
                    existingMarker?.position = latLng
                }
                val database = FirebaseDatabase.getInstance()
                val userReference = database.getReference("users/$userId")


                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val role = snapshot.child("role").getValue(String::class.java)
                        val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java)

                        if (role != null && evaluating!= null ) {


                                val existingMarker = markerMap[userId]
                                existingMarker?.remove()
                                markerMap.remove(userId)
                                addMarkerWithOptions(latLng, userId, evaluating, role)




                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar errores si es necesario
                    }


                })

            } else {
                val marker = markerMap[userId]
                if (marker != null) {
                    marker.remove()
                    markerMap.remove(userId)
                }

            }
        }
    }

    private fun addMarkerWithOptions(latLng: LatLng, userId: String, evaluating: Boolean, role: String) {




        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Marker")
            .icon(
                if (evaluating) {
                    BitmapDescriptorFactory.fromResource(R.mipmap.person_accept)
                } else if (role == "oferente") {
                    BitmapDescriptorFactory.fromResource(R.mipmap.car_iconv2)
                } else {
                    BitmapDescriptorFactory.fromResource(R.mipmap.person)
                }
            )


        obtenerRoleActual { currentUserRole ->
            val userRef = FirebaseDatabase.getInstance().getReference("users")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.key == userId) {
                            val userRole = userSnapshot.child("role").getValue(String::class.java)

                            if (userRole != currentUserRole) {
                                val existingMarker = markerMap[userId]


                                if (existingMarker != null) {
                                    existingMarker.position = latLng
                                } else {
                                    val nombre = userSnapshot.child("info/name").getValue(String::class.java)
                                    val carrera = userSnapshot.child("info/career").getValue(String::class.java)
                                    val genero = userSnapshot.child("info/gender").getValue(String::class.java)

                                    if (currentFilter != null && (currentFilter == "Hombre" || currentFilter == "Mujer" || currentFilter == "ieci" || currentFilter == "cpa" || currentFilter == "ici" || currentFilter == "ic")) {
                                        if (genero == currentFilter || carrera == currentFilter) {



                                            val marker = map.addMarker(markerOptions)
                                            marker?.tag = userId
                                            markerMap[userId] = marker
                                        }
                                    } else {

                                        val marker = map.addMarker(markerOptions)
                                        marker?.tag = userId
                                        markerMap[userId] = marker
                                    }
                                }
                            }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
    private fun obtenerRoleActual(callback: (String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().reference
            val userRef = databaseRef.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Obtener los valores de la base de datos
                        val rol = snapshot.child("role").getValue(String::class.java)

                        // Llamar a la devolución de llamada con el resultado
                        callback(rol.orEmpty()) // Se usa orEmpty para evitar valores nulos
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de error si es necesario
                }
            })
        }
    }

    private fun changeMarkerColor(userId: String, evaluating: Boolean,role: String?) {




        markerMap[userId]?.setIcon(
            if (evaluating){
                BitmapDescriptorFactory.fromResource(R.mipmap.person_accept)

            }else if(role == "oferente"){
                BitmapDescriptorFactory.fromResource(R.mipmap.car_iconv2)

            } else
                BitmapDescriptorFactory.fromResource(R.mipmap.person)
        )



    }

   // private var popup: PopUpFragment? = null
    private fun showMarkersFromDatabase(activity: AppCompatActivity?,rutaActual: Polyline?) {
        val coordinatesRef = FirebaseDatabase.getInstance().reference.child("coordinates")
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        coordinatesRef.addChildEventListener(object : ChildEventListener {
            @SuppressLint("PotentialBehaviorOverride")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    updateCoordinates(snapshot)

                    map.setOnMarkerClickListener { clickedMarker ->
                        val clickedUserId = clickedMarker.tag as? String
                        if (clickedUserId != null && clickedUserId!=currentUserId) {
                            Log.d("MarkadorClickeado", "MarkadorClickeado El macadroe fue clickeado")
                            otherUserId=clickedUserId


                               val popup=PopUpFragment()
                                popup.setUserId(currentUserId)
                                popup.targetUserIdDecide(otherUserId)
                                popup.show(activity!!.supportFragmentManager, "MostrarPopUp")
                                popup.dialog?.setOnDismissListener {
                                }

                        }
                        true
                    }
                    coordinatesRef.child(snapshot.key!!).onDisconnect().setValue(null)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                updateCoordinates(snapshot)
                coordinatesRef.child(snapshot.key!!).onDisconnect().setValue(null)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val userId = snapshot.key
                val marker = markerMap[userId]
                if (marker != null) {
                    marker.remove()
                    markerMap.remove(userId)
                }

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDatabase", "Error", error.toException())
            }
        })


        usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key
                val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java) ?: false
                val role = snapshot.child("role").getValue(String::class.java)


                val userCurrent = FirebaseAuth.getInstance().currentUser?.uid

                if (userId == userCurrent) {

                    Log.d("RoleValor", "El valor de role actual dela db es $role desde el added")

                    if (role == null) {
                        val intent = Intent(this@MainActivity, SeleccionRol::class.java)
                        startActivity(intent)
                       // finish()
                    }

                    changeMarkerColor(userId!!, evaluating,role)

                }




            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val userId = snapshot.key
                val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java) ?: false
                val role = snapshot.child("role").getValue(String::class.java)

                val userCurrent = FirebaseAuth.getInstance().currentUser?.uid

                Log.d("RoleValor", "El valor de role actual dela db es $role")


                if (userId == userCurrent) {

                    changeMarkerColor(userId!!, evaluating,role)
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {

                deleteCoordinatesFromDatabase()
              // FirebaseAuth.getInstance().signOut()
//                val intent = Intent(this@MainActivity, Login::class.java)
//                startActivity(intent)
                finish()
            }

        })




    }

    private lateinit var polyline: Polyline
    private var rutaActual: Polyline? = null
    private fun drawRoute(lat1: Double,long1: Double, lat2: Double, long2: Double ) {
        val origin = LatLng(lat1, long1)
        val destination = LatLng(lat2, long2)

        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key="

        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                val directions = JSONObject(response)
                val points = directions.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")


                val decodedPoints = PolyUtil.decode(points)


                polyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(decodedPoints)
                        .color(Color.BLACK)
                        .width(5f)
                )

                if (rutaActual != null) {
                    rutaActual!!.remove()
                }
                rutaActual = polyline

                updateEstimatedTime(lat1, long1, lat2, long2)


            },
            {}
        )
        {}

        Volley.newRequestQueue(this).add(request)

    }


    private fun updateEstimatedTime(lat1: Double, long1: Double, lat2: Double, long2: Double) {
        val origin = LatLng(lat1, long1)
        val destination = LatLng(lat2, long2)

        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=AIzaSyCFJx_C9_VLdH6NkYLl3BjJI5-76iSovIg"

        val request = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                val directions = JSONObject(response)
                val duration = directions.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONObject("duration")
                    .getString("text")


                val durationInMinutes = duration.split(" ")[0].toInt()

                val userId = firebaseAuth.currentUser?.uid

                if (userId != null) {

                    val evaluatingRef = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("evaluating")

                    evaluatingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val isEvaluating = dataSnapshot.getValue(Boolean::class.java) ?: false
                            if (durationInMinutes <= 3 && isEvaluating) {
                                isCalificarButtonEnabled = true
                                calificarButton.isEnabled = true
                              //  decidingUserId=null


                                evaluatingRef.setValue(false)


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle the error
                        }
                    })





                    val database = FirebaseDatabase.getInstance()
                    val userReference = database.getReference("users/$userId")
                    userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        @SuppressLint("SuspiciousIndentation")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val evaluating = snapshot.child("evaluating").getValue(Boolean::class.java)

                                if (evaluating==false) {
                                    // If evaluating is false, reset decidingUserId to null
                                    decidingUserId = null
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Manejar errores si es necesario
                        }


                    })



                }








                val tiempo = findViewById<TextView>(R.id.TimeArrive)
                tiempo.setText(durationInMinutes.toString())


            },
            {}
        ) {}

        Volley.newRequestQueue(this).add(request)
    }
    private fun insertOrUpdateCoordinatesToDatabase(latitude: Double, longitude: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val coordinates = hashMapOf(
                "latitud" to latitude,
                "longitud" to longitude
            )

            val coordinatesRef = FirebaseDatabase.getInstance().reference.child("coordinates")
            coordinatesRef.child(userId).setValue(coordinates)

        }
    }

    private var decidingUserId: String? = null

    private fun listenForMessages() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        userId?.let {
            val usersRef = FirebaseDatabase.getInstance().reference.child("users")
            val evaluatingRef = usersRef.child(it).child("evaluating")
            evaluatingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists() || dataSnapshot.getValue(Boolean::class.java) == null) {
                        evaluatingRef.setValue(false)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

            val messagesRef = usersRef.child(it).child("messages")
            messagesRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageData = snapshot.value as? Map<*, *>
                    val fromUserId = messageData?.get("fromUserId") as? String
                    evaluatingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (messageData?.get("type") == "open_decide") {

                                    decidingUserId = fromUserId


                                    val decideFragment= DecideFragment()
                                    if(!supportFragmentManager.isStateSaved && !isFinishing){
                                        decideFragment.setUserId( userId )
                                        decideFragment.setReUser( fromUserId!! )
                                        decideFragment.show(supportFragmentManager, "decideFragment")
                                        decideFragment.dialog?.setOnDismissListener {
                                            // decideFragment = null
                                        }

                                    }




                                  //  }
                                     //   userIdList.add(fromUserId!!)
                                    calificarButton.setOnClickListener {
                                        if (calificarButton.isEnabled) {
                                            val repu = RepuFragment()
                                            repu.setCalificarButton(calificarButton)
                                            repu.setUserIds(userId, fromUserId!!)
                                            repu.show(supportFragmentManager, "FragmentRepu")

                                        }
                                    }
                                    evaluatingRef.setValue(true)
                                    val mensajeRef = messagesRef.child(snapshot.key!!)
                                    mensajeRef.removeValue()

                                } else if (messageData?.get("type") == "open_decide_Resp") {



                                    val confirmFragment= ConfirmFragment()

                               // if (confirmFragment == null) {
                                    //confirmFragment = ConfirmFragment()
                                    if(!supportFragmentManager.isStateSaved && !isFinishing) {
                                        confirmFragment.setUserId(userId)
                                        confirmFragment.setReUser(fromUserId!!)
                                        confirmFragment.show(
                                            supportFragmentManager,
                                            "confirmFragment"
                                        )
                                        confirmFragment.dialog?.setOnDismissListener {
                                        //    confirmFragment = null
                                        }
                                        //  }
                                    }

                                calificarButton.setOnClickListener {
                                    if (calificarButton.isEnabled) {
                                        val repu = RepuFragment()
                                        repu.setCalificarButton(calificarButton)
                                        repu.setUserIds(userId, fromUserId!!)
                                        repu.show(supportFragmentManager, "FragmentRepu")

                                    }
                                }

                                evaluatingRef.setValue(true)

                                val mensajeRef = messagesRef.child(snapshot.key!!)
                                mensajeRef.removeValue()


//                                    if() {
//
//                                    }

                            }
                        }


                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabase", "Error listening for messages", error.toException())
                }
            })
        }



    }


    override fun onCalificarButtonEnabled(enabled: Boolean) {
        calificarButton.isEnabled = enabled

    }

    fun mostrarDialogoFiltro(item: MenuItem) {

        val filtroDialogFragment = Filters()
        filtroDialogFragment.setFiltroDialogListener(this)
        filtroDialogFragment.show(supportFragmentManager, "FiltroDialogFragment")

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
