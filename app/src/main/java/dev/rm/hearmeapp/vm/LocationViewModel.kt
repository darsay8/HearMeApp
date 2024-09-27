package dev.rm.hearmeapp.vm

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Initial)
    val locationState: StateFlow<LocationState> = _locationState

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun initializeFusedLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }


    @OptIn(ExperimentalPermissionsApi::class)
    fun requestLocationPermissions(permissionsState: MultiplePermissionsState) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            _locationState.value = LocationState.Success(
                                LatLng(location.latitude, location.longitude)
                            )
                        } else {
                            _locationState.value =
                                LocationState.Error("Unable to get location. Please try again.")
                        }
                    }
                    .addOnFailureListener { e ->
                        _locationState.value =
                            LocationState.Error("Error getting location: ${e.message}")
                    }
            } catch (e: Exception) {
                _locationState.value = LocationState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun getShareableLocation(): String {
        return when (val state = _locationState.value) {
            is LocationState.Success -> {
                "https://www.google.com/maps/search/?api=1&query=${state.location.latitude},${state.location.longitude}"
            }

            is LocationState.Error -> {
                "My location is currently unavailable."
            }

            else -> "Location data not available."
        }
    }
}

sealed class LocationState {
    object Initial : LocationState()
    object Loading : LocationState()
    data class Success(val location: LatLng) : LocationState()
    data class Error(val message: String) : LocationState()
}