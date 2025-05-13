//
//  MapViewController.swift
//  
//
//  Created by Yusuf Teker on 13.05.2025.
//

import UIKit
import MapKit

class MapViewController: UIViewController {
    let mapView = MKMapView()

    var onLocationSelected: ((Double, Double) -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()
        view = mapView

        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        mapView.addGestureRecognizer(tapGesture)
    }

    func setInitialLocation(_ location: LocationUiModel?) {
        guard let location = location else { return }
        let coordinate = CLLocationCoordinate2D(latitude: location.lat, longitude: location.lng)
        let region = MKCoordinateRegion(center: coordinate, latitudinalMeters: 1000, longitudinalMeters: 1000)
        mapView.setRegion(region, animated: false)
    }

    @objc private func handleTap(_ gestureRecognizer: UITapGestureRecognizer) {
        let point = gestureRecognizer.location(in: mapView)
        let coordinate = mapView.convert(point, toCoordinateFrom: mapView)
        onLocationSelected?(coordinate.latitude, coordinate.longitude)
    }
}
