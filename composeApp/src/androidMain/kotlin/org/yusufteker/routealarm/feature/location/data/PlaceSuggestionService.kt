package org.yusufteker.routealarm.feature.location.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.yusufteker.routealarm.BuildConfig
import org.yusufteker.routealarm.feature.location.domain.Location
import org.yusufteker.routealarm.feature.location.domain.Place
import org.yusufteker.routealarm.feature.location.domain.emptyLocation
import java.util.Locale
import kotlin.coroutines.resume

actual class PlaceSuggestionService(
    private val context: Context,
    httpClient: HttpClient
) {
    private val nominatimClient = NominatimPlaceClient(httpClient)

    @Volatile
    private var placesUnavailableForSession = BuildConfig.GOOGLE_MAPS_API_KEY.isBlank()

    private val placesClient: PlacesClient? by lazy {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY
        if (key.isBlank()) return@lazy null
        try {
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(context, key)
            }
            Places.createClient(context)
        } catch (e: Exception) {
            Napier.e("Places init failed: ${e.message}", tag = "PlaceSuggestion")
            null
        }
    }

    actual suspend fun getSuggestions(query: String, nearLocation: Location?): List<Place> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        if (!placesUnavailableForSession) {
            val googleResults = fetchGoogleSuggestions(trimmed, nearLocation)
            if (googleResults.isNotEmpty()) return googleResults
        }

        return coroutineScope {
            val freePlacesDeferred = async { nominatimClient.searchFreePlaces(trimmed, nearLocation) }
            val geocoderDeferred = async { searchWithAndroidGeocoder(trimmed, nearLocation) }
            mergeAndRankPlaces(
                places = geocoderDeferred.await() + freePlacesDeferred.await(),
                query = trimmed,
                nearLocation = nearLocation
            )
        }
    }

    actual suspend fun getPlaceDetails(placeId: String): Place? {
        val client = placesClient ?: return null
        val request = FetchPlaceRequest.newInstance(
            placeId,
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
        )

        return suspendCancellableCoroutine { cont ->
            client.fetchPlace(request)
                .addOnSuccessListener { response ->
                    if (!cont.isActive) return@addOnSuccessListener
                    val place = response.place
                    cont.resume(
                        Place(
                            id = place.id ?: placeId,
                            name = place.name ?: "",
                            address = place.address ?: "",
                            latitude = place.latLng?.latitude ?: 0.0,
                            longitude = place.latLng?.longitude ?: 0.0
                        )
                    )
                }
                .addOnFailureListener { e ->
                    Napier.e("FetchPlace failed: ${e.message}", tag = "PlaceSuggestion")
                    if (cont.isActive) cont.resume(null)
                }
        }
    }

    actual suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        val nominatimName = nominatimClient.reverseGeocode(lat, lng)
        if (!nominatimName.isNullOrBlank()) return nominatimName
        return reverseWithAndroidGeocoder(lat, lng)
    }

    private suspend fun fetchGoogleSuggestions(query: String, nearLocation: Location?): List<Place> {
        val client = placesClient ?: return emptyList()
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)

        if (nearLocation != null && (nearLocation.lat != emptyLocation.lat || nearLocation.lng != emptyLocation.lng)) {
            val delta = 0.25
            requestBuilder.setLocationBias(
                RectangularBounds.newInstance(
                    LatLng(nearLocation.lat - delta, nearLocation.lng - delta),
                    LatLng(nearLocation.lat + delta, nearLocation.lng + delta)
                )
            )
            requestBuilder.setOrigin(LatLng(nearLocation.lat, nearLocation.lng))
        }

        val request = requestBuilder.build()

        return suspendCancellableCoroutine { cont ->
            client.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    if (!cont.isActive) return@addOnSuccessListener
                    val places = response.autocompletePredictions.take(8).map { prediction ->
                        Place(
                            id = prediction.placeId,
                            name = prediction.getPrimaryText(null).toString(),
                            address = prediction.getSecondaryText(null).toString(),
                            latitude = emptyLocation.lat,
                            longitude = emptyLocation.lng
                        )
                    }
                    cont.resume(places)
                }
                .addOnFailureListener { e ->
                    val statusCode = (e as? ApiException)?.statusCode
                    if (statusCode == BILLING_NOT_ENABLED_CODE || statusCode == PlacesStatusCodes.REQUEST_DENIED) {
                        placesUnavailableForSession = true
                    }
                    Napier.w("Google Places failed ($statusCode), falling back to OpenStreetMap/Geocoder: ${e.message}", tag = "PlaceSuggestion")
                    if (cont.isActive) cont.resume(emptyList())
                }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun searchWithAndroidGeocoder(
        query: String,
        nearLocation: Location?
    ): List<Place> = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext emptyList()
        val geocoder = Geocoder(context, Locale.getDefault())
        val validCenter = nearLocation?.takeIf {
            it.lat != emptyLocation.lat || it.lng != emptyLocation.lng
        }

        suspend fun runGeocode(bounded: Boolean): List<Address> {
            val delta = 0.5
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    val listener = object : Geocoder.GeocodeListener {
                        override fun onGeocode(results: MutableList<Address>) {
                            if (cont.isActive) cont.resume(results)
                        }

                        override fun onError(errorMessage: String?) {
                            if (cont.isActive) cont.resume(emptyList())
                        }
                    }
                    if (bounded && validCenter != null) {
                        geocoder.getFromLocationName(
                            query,
                            8,
                            (validCenter.lat - delta).coerceAtLeast(-90.0),
                            (validCenter.lng - delta).coerceAtLeast(-180.0),
                            (validCenter.lat + delta).coerceAtMost(90.0),
                            (validCenter.lng + delta).coerceAtMost(180.0),
                            listener
                        )
                    } else {
                        geocoder.getFromLocationName(query, 8, listener)
                    }
                }
            } else {
                if (bounded && validCenter != null) {
                    geocoder.getFromLocationName(
                        query,
                        8,
                        (validCenter.lat - delta).coerceAtLeast(-90.0),
                        (validCenter.lng - delta).coerceAtLeast(-180.0),
                        (validCenter.lat + delta).coerceAtMost(90.0),
                        (validCenter.lng + delta).coerceAtMost(180.0)
                    ).orEmpty()
                } else {
                    geocoder.getFromLocationName(query, 8).orEmpty()
                }
            }
        }

        try {
            val boundedResults = if (validCenter != null) runGeocode(bounded = true) else emptyList()
            val addresses = if (boundedResults.isNotEmpty()) boundedResults else runGeocode(bounded = false)

            addresses.mapIndexedNotNull { index, addr ->
                if (!addr.hasLatitude() || !addr.hasLongitude()) return@mapIndexedNotNull null
                val title = addr.featureName?.takeIf { it.isNotBlank() && it != addr.thoroughfare }
                    ?: addr.thoroughfare?.takeIf { it.isNotBlank() }
                    ?: addr.subLocality?.takeIf { it.isNotBlank() }
                    ?: addr.locality?.takeIf { it.isNotBlank() }
                    ?: query
                val formattedAddress = listOfNotNull(
                    addr.thoroughfare?.takeIf { it.isNotBlank() && it != title },
                    addr.subLocality?.takeIf { it.isNotBlank() && it != title },
                    addr.locality?.takeIf { it.isNotBlank() },
                    addr.adminArea?.takeIf { it.isNotBlank() },
                    addr.countryName?.takeIf { it.isNotBlank() }
                ).distinct().joinToString(", ")

                Place(
                    id = "geocoder:$index:${addr.latitude},${addr.longitude}",
                    name = title,
                    address = formattedAddress,
                    latitude = addr.latitude,
                    longitude = addr.longitude
                )
            }
        } catch (e: Exception) {
            Napier.w("Android Geocoder search failed: ${e.message}", tag = "PlaceSuggestion")
            emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseWithAndroidGeocoder(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(results: MutableList<Address>) {
                            if (cont.isActive) cont.resume(results)
                        }

                        override fun onError(errorMessage: String?) {
                            if (cont.isActive) cont.resume(emptyList())
                        }
                    })
                }
            } else {
                geocoder.getFromLocation(lat, lng, 1).orEmpty()
            }

            val addr = addresses.firstOrNull() ?: return@withContext null
            listOfNotNull(
                addr.thoroughfare?.takeIf { it.isNotBlank() } ?: addr.featureName?.takeIf { it.isNotBlank() },
                addr.subLocality?.takeIf { it.isNotBlank() } ?: addr.locality?.takeIf { it.isNotBlank() }
            ).distinct().joinToString(", ").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Napier.w("Android Geocoder reverse failed: ${e.message}", tag = "PlaceSuggestion")
            null
        }
    }

    companion object {
        private const val BILLING_NOT_ENABLED_CODE = 9011
    }
}
