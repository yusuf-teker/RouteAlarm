package org.yusufteker.routealarm.feature.location.data

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import io.github.aakira.napier.Napier
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yusufteker.routealarm.BuildConfig
import org.yusufteker.routealarm.feature.location.domain.Place
import kotlin.coroutines.resume

// androidMain
actual class PlaceSuggestionService(private val context: Context) {
    private val placesClient: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    actual suspend fun getSuggestions(query: String): List<Place> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        return suspendCancellableCoroutine { cont ->
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions = response.autocompletePredictions

                    kotlinx.coroutines.GlobalScope.launch {
                        val places = predictions.mapNotNull { prediction ->
                            try {
                                getPlaceDetails(prediction.placeId)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        cont.resume(places)
                    }
                }
                .addOnFailureListener {
                    cont.resume(emptyList(), null)
                    Napier.e ("PlaceSuggesion service failed", tag = "Yusuf")
                }
        }
    }

    actual suspend fun getPlaceDetails(placeId: String): Place? {
        val request = FetchPlaceRequest.newInstance(
            placeId,
            listOf(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.ADDRESS, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
        )

        return suspendCancellableCoroutine { cont ->
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    cont.resume(
                        Place(
                            id = place.id ?: "",
                            name = place.name ?: "",
                            address = place.address ?: "",
                            latitude = place.latLng?.latitude ?: 0.0,
                            longitude = place.latLng?.longitude ?: 0.0
                        ),
                        null
                    )
                }
                .addOnFailureListener {
                    cont.resume(null, null)
                }
        }
    }
}