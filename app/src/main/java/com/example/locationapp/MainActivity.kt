package com.example.locationapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationAppTheme {
                // A surface container using the 'background' color from the theme

                val viewModel: LocationViewModel = viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel : LocationViewModel){
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    LocationDisplay(locationUtils = locationUtils,viewModel, context =context )
}




//To ask for location permission from user at the main activity
@Composable
fun LocationDisplay(locationUtils: LocationUtils , viewModel: LocationViewModel, context : Context ){

    val location = viewModel.location.value

    val address = location?.let {
        locationUtils.reverseGeoCodeLocation(location)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(contract =ActivityResultContracts.RequestMultiplePermissions(),
        onResult ={permissions->
            if(permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                //ACCESS TO THE LOCATION
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            }
            else{
                //ASK FOR PERMISSION
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)

                if(rationalRequired){
                    Toast.makeText(context, "Location Feature is required for this feature to work" , Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(context, "Location Feature is required, Please enable it from settings" , Toast.LENGTH_SHORT).show()
                }
            }
        })

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if(location!= null){
            Text("Location: Latitude= ${location.latitude} && Longitude = ${location.longitude} \n $address")
        }
        else {
            Text("Location Not Available")
        }
        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                //Permission already Granted
                locationUtils.requestLocationUpdates(viewModel)
            } else {
                //Request location Permission
                // PRECISE LOCATION AND APPROXIMATE LOCATION
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }) {
            Text("Get Location")
        }

    }
}