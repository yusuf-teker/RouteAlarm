package org.yusufteker.routealarm.feature.location.domain

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import org.yusufteker.routealarm.BuildConfig
import com.google.android.libraries.places.api.model.Place.Field

// androidMain
actual class PlaceSuggestionService(private val context: Context) {
    private val placesClient: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    actual suspend fun getSuggestions(query: String): List<Place> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        return suspendCancellableCoroutine { cont ->
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val result = response.autocompletePredictions.map {
                        Place(
                            id = it.placeId,
                            name = it.getPrimaryText(null).toString(),
                            address = it.getSecondaryText(null).toString(),
                            latitude = 0.0,
                            longitude = 0.0
                        )
                    }
                    cont.resume(result, null)
                }
                .addOnFailureListener {
                    cont.resume(emptyList(), null)
                }
        }
    }

    actual suspend fun getPlaceDetails(placeId: String): Place? {
        val request = FetchPlaceRequest.newInstance(
            placeId,
            listOf(Field.ID, Field.NAME, Field.ADDRESS, Field.LAT_LNG)
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
