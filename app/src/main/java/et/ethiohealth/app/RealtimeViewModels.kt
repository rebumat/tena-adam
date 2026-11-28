package et.ethiohealth.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * v1.3.0 view models for the live screens. Each holds loading / error /
 * offline state; the repository never throws to the UI, so a failed network
 * call simply shows cached or demo data with an offline notice.
 */
class LiveNearbyViewModel(private val store: Store) : ViewModel() {
    var facilities: List<LiveFacility> by mutableStateOf(emptyList())
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    var isOffline: Boolean by mutableStateOf(false)
        private set
    var isCached: Boolean by mutableStateOf(false)
        private set
    var placeName: String? by mutableStateOf<String?>(null)
        private set

    fun load(lat: Double, lon: Double) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            try {
                placeName = RealtimeRepository.reverseGeocode(lat, lon)
                when (val result = RealtimeRepository.fetchNearby(lat, lon, store)) {
                    is LiveResult.Live -> {
                        facilities = result.data; isOffline = false; isCached = false
                    }
                    is LiveResult.Cached -> {
                        facilities = result.data; isOffline = true; isCached = true
                    }
                    is LiveResult.Demo -> {
                        facilities = result.data; isOffline = true; isCached = false
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}

class MedicineInfoViewModel : ViewModel() {
    var results: List<MedicineInfo> by mutableStateOf(emptyList())
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    var searched: Boolean by mutableStateOf(false)
        private set

    fun search(name: String) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            try {
                when (val result = RealtimeRepository.searchMedicine(name)) {
                    is LiveResult.Live -> results = result.data
                    is LiveResult.Cached -> results = result.data
                    is LiveResult.Demo -> results = result.data
                }
                searched = true
            } finally {
                isLoading = false
            }
        }
    }
}

class AirWeatherViewModel(private val store: Store) : ViewModel() {
    var data: AirWeather? by mutableStateOf<AirWeather?>(null)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    var isOffline: Boolean by mutableStateOf(false)
        private set
    var isCached: Boolean by mutableStateOf(false)
        private set

    fun load() {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            try {
                when (val result = RealtimeRepository.fetchAirWeather(store = store)) {
                    is LiveResult.Live -> {
                        data = result.data; isOffline = false; isCached = false
                    }
                    is LiveResult.Cached -> {
                        data = result.data; isOffline = true; isCached = true
                    }
                    is LiveResult.Demo -> {
                        data = result.data; isOffline = true; isCached = false
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}

/** Factory so the screens can pass the on-device [Store] into the view models. */
class RealtimeViewModelFactory(private val store: Store) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LiveNearbyViewModel::class.java) ->
                LiveNearbyViewModel(store) as T
            modelClass.isAssignableFrom(AirWeatherViewModel::class.java) ->
                AirWeatherViewModel(store) as T
            modelClass.isAssignableFrom(MedicineInfoViewModel::class.java) ->
                MedicineInfoViewModel() as T
            else -> throw IllegalArgumentException("Unknown view model: ${modelClass.name}")
        }
    }
}
