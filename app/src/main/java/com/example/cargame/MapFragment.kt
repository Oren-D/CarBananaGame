package com.example.cargame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        parentFragmentManager.setFragmentResultListener("SCORE_CHANNEL", viewLifecycleOwner) { _, bundle ->
            val json = bundle.getString("SCORE_DATA")
            if (json != null) {
                val scoreObj = gson.fromJson(json, Score::class.java)
                zoomToLocation(scoreObj)
            }
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun zoomToLocation(score: Score) {
        if (::googleMap.isInitialized) {
            val location = LatLng(score.lat, score.lon)

            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Score: ${score.score}")
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
        }
    }
}