//
//  GoogleMapView.swift
//  iosApp
//
//  Created by Yusuf Teker on 16.05.2025.
//  Copyright © 2025 orgName. All rights reserved.
//import SwiftUI
import GoogleMaps
import ComposeApp
import SwiftUI


// Kotlin arayüzünü implement eden protocol
@objc class KotlinMapController: NSObject, MapController {
    private var mapState: GoogleMapState
    
    init(mapState: GoogleMapState) {
        self.mapState = mapState
        super.init()
    }
    
    func updateSelectedLocation(location: Location) {
        self.mapState.updateSelectedLocation(location)
    }
    
    func centerToLocation(location: Location) {
        self.mapState.centerToLocation(location)
    }
}

class IOSPlatformMapFactory: PlatformMapFactory {
    static var shared = IOSPlatformMapFactory()
    
    func createMapView(initialLocation: Location, onMapClick: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        // ObservableObject'i oluşturup varsayılan konum ile başlat
        let mapState = GoogleMapState(initialLocation: initialLocation)
        
        // SwiftUI view'ini UIViewController olarak döndür
        let view = GoogleMapView(
            mapState: mapState,
            onTap: { latitude, longitude in
                onMapClick(KotlinDouble(value: latitude), KotlinDouble(value: longitude))
            }
        )
        
        // UIHostingController'ı oluştur ve mapState'i kaydet
        let hostingController = UIHostingController(rootView: view)
        
        // Kotlin tarafından erişilebilir controller objesi oluştur
        let mapController = KotlinMapController(mapState: mapState)
        
        // Controller'ı NSObject extension ile UIViewController'a bağla
        objc_setAssociatedObject(hostingController, &AssociatedKeys.mapControllerKey, mapController, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        
        return hostingController
    }
}

// Associated objects için key
private struct AssociatedKeys {
    static var mapControllerKey = "mapControllerKey"
}

// UIViewController extension
extension UIViewController {
    var mapController: KotlinMapController? {
        get {
            return objc_getAssociatedObject(self, &AssociatedKeys.mapControllerKey) as? KotlinMapController
        }
    }
}

// Harita state'i için observable class
class GoogleMapState: ObservableObject {
    @Published var selectedLocation: Location
    @Published var centerToLocation: Location?
    
    init(initialLocation: Location) {
        self.selectedLocation = initialLocation
    }
    
    func updateSelectedLocation(_ location: Location) {
        self.selectedLocation = location
    }
    
    func centerToLocation(_ location: Location) {
        self.centerToLocation = location
    }
}

// SwiftUI View
struct GoogleMapView: UIViewRepresentable {
    @ObservedObject var mapState: GoogleMapState
    var onTap: ((Double, Double) -> Void)?
    
    func makeCoordinator() -> Coordinator {
        return Coordinator(mapState: mapState, onTap: onTap)
    }

    func makeUIView(context: Context) -> GMSMapView {
        let camera = GMSCameraPosition.camera(
            withLatitude: mapState.selectedLocation.lat,
            longitude: mapState.selectedLocation.lng,
            zoom: 15
        )

        let options = GMSMapViewOptions()
        options.camera = camera

        let mapView = GMSMapView(options: options)
        mapView.delegate = context.coordinator

        // Harita ayarları
        mapView.isMyLocationEnabled = true
        mapView.settings.myLocationButton = true
        
        // Başlangıç marker'ı ekle
        let initialMarker = GMSMarker(position: CLLocationCoordinate2D(
            latitude: mapState.selectedLocation.lat,
            longitude: mapState.selectedLocation.lng)
        )
        initialMarker.title = mapState.selectedLocation.name
        initialMarker.map = mapView
        context.coordinator.marker = initialMarker

        context.coordinator.mapView = mapView
        return mapView
    }

    func updateUIView(_ uiView: GMSMapView, context: Context) {
        // Seçilen konum değiştiğinde haritayı güncelle
        let position = CLLocationCoordinate2D(
            latitude: mapState.selectedLocation.lat,
            longitude: mapState.selectedLocation.lng
        )
        
        // Marker'ı güncelle
        if let marker = context.coordinator.marker {
            marker.position = position
            marker.title = mapState.selectedLocation.name
        } else {
            let newMarker = GMSMarker(position: position)
            newMarker.title = mapState.selectedLocation.name
            newMarker.map = uiView
            context.coordinator.marker = newMarker
        }
        
        // Mevcut konuma odaklanma isteği varsa işle
        if let centerLocation = mapState.centerToLocation {
            let centerPosition = CLLocationCoordinate2D(
                latitude: centerLocation.lat,
                longitude: centerLocation.lng
            )
            
            let camera = GMSCameraUpdate.setCamera(
                GMSCameraPosition.camera(
                    withLatitude: centerPosition.latitude,
                    longitude: centerPosition.longitude,
                    zoom: 15
                )
            )
            uiView.animate(with: camera)
            
            // İşlendikten sonra null'a çek
            DispatchQueue.main.async {
                mapState.centerToLocation = nil
            }
        }
        
        // Seçilen konuma odaklan
        let camera = GMSCameraUpdate.setCamera(
            GMSCameraPosition.camera(
                withLatitude: position.latitude,
                longitude: position.longitude,
                zoom: 15
            )
        )
        uiView.animate(with: camera)
    }

    class Coordinator: NSObject, GMSMapViewDelegate {
        @ObservedObject var mapState: GoogleMapState
        let onTap: ((Double, Double) -> Void)?
        weak var mapView: GMSMapView?
        var marker: GMSMarker?

        init(mapState: GoogleMapState, onTap: ((Double, Double) -> Void)?) {
            self.mapState = mapState
            self.onTap = onTap
        }

        func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
            // Seçilen konuma yeni marker yerleştir
            if let existingMarker = marker {
                existingMarker.position = coordinate
            } else {
                let newMarker = GMSMarker(position: coordinate)
                newMarker.title = "Seçilen Konum"
                newMarker.map = mapView
                marker = newMarker
            }

            // Kamerayı animasyonla konuma götür
            let camera = GMSCameraUpdate.setCamera(
                GMSCameraPosition.camera(
                    withLatitude: coordinate.latitude,
                    longitude: coordinate.longitude,
                    zoom: 15
                )
            )
            mapView.animate(with: camera)

            // Kotlin tarafını bilgilendir
            onTap?(coordinate.latitude, coordinate.longitude)
            
            // MapState'i güncelle
            let newLocation = Location(name: "Seçilen Konum", lat: coordinate.latitude, lng: coordinate.longitude)
            DispatchQueue.main.async {
                self.mapState.selectedLocation = newLocation
            }
        }
    }
}
