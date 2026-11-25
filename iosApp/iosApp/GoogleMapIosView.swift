import SwiftUI
import GoogleMaps
import CoreLocation
import ComposeApp

class IOSNativeViewFactory: NSObject, UpdatableMapViewFactory {

    static var shared = IOSNativeViewFactory()
    private var mapViewModel: MapViewModel?

    func createGoogleMapView(
        selectedLocation: Location?,
        currentLocation: Location,
        onLocationSelected: @escaping (Location) -> Void,
        centerToCurrentLocation: Bool,
        onCenterLocationConsumed: @escaping () -> Void
    ) -> UIViewController {
        let viewModel = MapViewModel(
            currentLocation: currentLocation,
            selectedLocation: selectedLocation,
            centerToCurrentLocation: centerToCurrentLocation
        )
        self.mapViewModel = viewModel

        let view = GoogleMapIosView(
            viewModel: viewModel,
            onLocationSelected: onLocationSelected,
            onCenterLocationConsumed: onCenterLocationConsumed
        )
        let hostingController = UIHostingController(rootView: view.edgesIgnoringSafeArea(.all))
        hostingController.view.frame = UIScreen.main.bounds
        hostingController.view.insetsLayoutMarginsFromSafeArea = false
        hostingController.additionalSafeAreaInsets = .zero

        return hostingController
    }

    func updateCurrentLocation(location: Location) {
        mapViewModel?.updateCurrentLocation(location)
    }

    func updateSelectedLocation(location: Location?) {
        mapViewModel?.updateSelectedLocation(location)
    }

    func updateCenterToCurrentLocation(shouldCenter: Bool) {
        mapViewModel?.updateCenterToCurrentLocation(shouldCenter)
    }
}

struct GoogleMapIosView: UIViewRepresentable {

    @ObservedObject var viewModel: MapViewModel
    var onLocationSelected: ((Location) -> Void)?
    var onCenterLocationConsumed: (() -> Void)?

    private static let fallbackCoordinate = CLLocationCoordinate2D(latitude: 41.0082, longitude: 28.9784)

    func makeUIView(context: Context) -> GMSMapView {
        let mapView = GMSMapView()

        let hasValidCurrent = !isEmptyLocation(viewModel.currentLocation)
        let initialCoord = hasValidCurrent
            ? CLLocationCoordinate2D(latitude: viewModel.currentLocation.lat, longitude: viewModel.currentLocation.lng)
            : Self.fallbackCoordinate

        mapView.camera = GMSCameraPosition.camera(withTarget: initialCoord, zoom: 15.0)
        mapView.delegate = context.coordinator
        mapView.settings.zoomGestures = true
        mapView.settings.scrollGestures = true
        mapView.settings.rotateGestures = true
        mapView.settings.tiltGestures = true
        mapView.settings.myLocationButton = false

        let authStatus = CLLocationManager().authorizationStatus
        mapView.isMyLocationEnabled = (authStatus == .authorizedWhenInUse || authStatus == .authorizedAlways)

        if hasValidCurrent {
            context.coordinator.hasCenteredInitially = true
            context.coordinator.lastCurrentLocation = viewModel.currentLocation
        }

        if let selected = viewModel.selectedLocation, !isEmptyLocation(selected) {
            addMarker(on: mapView, at: selected)
            context.coordinator.lastSelectedLocation = selected
        }

        return mapView
    }

    func updateUIView(_ mapView: GMSMapView, context: Context) {
        let authStatus = CLLocationManager().authorizationStatus
        let locationAuthorized = (authStatus == .authorizedWhenInUse || authStatus == .authorizedAlways)
        if mapView.isMyLocationEnabled != locationAuthorized {
            mapView.isMyLocationEnabled = locationAuthorized
        }

        if !context.coordinator.hasCenteredInitially && !isEmptyLocation(viewModel.currentLocation) {
            context.coordinator.hasCenteredInitially = true
            context.coordinator.lastCurrentLocation = viewModel.currentLocation
            animateCamera(mapView: mapView, to: viewModel.currentLocation, zoom: 15.0)
        }

        if viewModel.centerToCurrentLocation {
            if !isEmptyLocation(viewModel.currentLocation) {
                animateCamera(mapView: mapView, to: viewModel.currentLocation, zoom: 15.0)
            }
            DispatchQueue.main.async {
                viewModel.centerToCurrentLocation = false
                onCenterLocationConsumed?()
            }
        }

        let selected = viewModel.selectedLocation
        if let selected = selected, !isEmptyLocation(selected) {
            if context.coordinator.lastSelectedLocation?.lat != selected.lat ||
               context.coordinator.lastSelectedLocation?.lng != selected.lng ||
               context.coordinator.lastSelectedLocation?.name != selected.name {
                mapView.clear()
                addMarker(on: mapView, at: selected)
                animateCamera(mapView: mapView, to: selected, zoom: 15.0)
                context.coordinator.lastSelectedLocation = selected
            }
        } else if context.coordinator.lastSelectedLocation != nil {
            mapView.clear()
            context.coordinator.lastSelectedLocation = nil
        }
    }

    private func isEmptyLocation(_ location: Location) -> Bool {
        return location.lat == 0.0 && location.lng == 0.0
    }

    private func animateCamera(mapView: GMSMapView, to location: Location, zoom: Float) {
        let camera = GMSCameraUpdate.setCamera(GMSCameraPosition(
            latitude: location.lat,
            longitude: location.lng,
            zoom: zoom
        ))
        mapView.animate(with: camera)
    }

    func addMarker(on mapView: GMSMapView, at location: Location) {
        let marker = GMSMarker()
        marker.position = CLLocationCoordinate2D(latitude: location.lat, longitude: location.lng)
        if !location.name.isEmpty {
            marker.title = location.name
        }
        marker.map = mapView
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, GMSMapViewDelegate {
        var parent: GoogleMapIosView
        var hasCenteredInitially = false
        var lastSelectedLocation: Location?
        var lastCurrentLocation: Location?

        init(_ parent: GoogleMapIosView) {
            self.parent = parent
            super.init()
        }

        func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
            selectCoordinate(mapView: mapView, coordinate: coordinate, name: "")
        }

        func mapView(_ mapView: GMSMapView, didTapPOIWithPlaceID placeID: String, name: String, location: CLLocationCoordinate2D) {
            let cleanName = name.replacingOccurrences(of: "\n", with: " ").trimmingCharacters(in: .whitespacesAndNewlines)
            selectCoordinate(mapView: mapView, coordinate: location, name: cleanName)
        }

        func mapView(_ mapView: GMSMapView, didLongPressAt coordinate: CLLocationCoordinate2D) {
            selectCoordinate(mapView: mapView, coordinate: coordinate, name: "")
        }

        private func selectCoordinate(mapView: GMSMapView, coordinate: CLLocationCoordinate2D, name: String) {
            let location = Location(name: name, lat: coordinate.latitude, lng: coordinate.longitude)
            mapView.clear()
            parent.addMarker(on: mapView, at: location)

            let targetZoom = max(mapView.camera.zoom, 15.0)
            let cameraUpdate = GMSCameraUpdate.setTarget(coordinate, zoom: targetZoom)
            mapView.animate(with: cameraUpdate)

            lastSelectedLocation = location
            parent.onLocationSelected?(location)
        }
    }
}
