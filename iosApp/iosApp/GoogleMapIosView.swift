    //
    //  GoogleMapIosView.swift
    //  iosApp
    //
    //  Created by Yusuf Teker on 21.05.2025.
    //  Copyright © 2025 orgName. All rights reserved.
    //

import SwiftUI
import GoogleMaps
import CoreLocation
import ComposeApp

class IOSNativeViewFactory: NSObject, UpdatableMapViewFactory {
        
        static var shared = IOSNativeViewFactory()
        private var mapViewModel: MapViewModel?

        func createGoogleMapView(selectedLocation: Location?, currentLocation:  Location, onLocationSelected: @escaping (Location) -> Void, centerToCurrentLocation: Bool, onCenterLocationConsumed: @escaping () -> Void) -> UIViewController {
            
        
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
            return UIHostingController(rootView: view)
        }
    
    // UpdatableMapViewFactory protokolünü implement et
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
        
        private let emptyLocation: Location = Location(name: "", lat: 42.0, lng: 42.0)
        
        func makeUIView(context: Context) -> GMSMapView {
            let mapView = GMSMapView()
            
            // Configure the initial camera position
            let camera = GMSCameraPosition.camera(
                withLatitude: viewModel.currentLocation.lat,
                longitude: viewModel.currentLocation.lng,
                zoom: 15.0
            )
            print("MAKEUIVIEW CAMERA",viewModel.currentLocation.lat , viewModel.currentLocation.lng)
            NSLog("MAKEUIVIEW cCAMERA: \(viewModel.currentLocation.lat), \(viewModel.currentLocation.lng)")
            mapView.camera = camera
            
            mapView.delegate = context.coordinator
            
            mapView.settings.zoomGestures = true
            mapView.settings.scrollGestures = true
            mapView.settings.rotateGestures = true
            mapView.settings.tiltGestures = true
            mapView.isMyLocationEnabled = true

            context.coordinator.lastCurrentLocation = viewModel.currentLocation
            context.coordinator.logMapLoaded()
            
            return mapView
        }
        
        func updateUIView(_ mapView: GMSMapView, context: Context) {
            NSLog("updateUIView called with: \(viewModel.currentLocation.lat), \(viewModel.currentLocation.lng)")
            if let last = context.coordinator.lastCurrentLocation {
                NSLog("Last location was: \(last.lat), \(last.lng)")
            } else {
                NSLog("No last location recorded")
            }
            // Update current location if changed
            if !isEmptyLocation(viewModel.currentLocation) &&
                ( isEmptyLocation(context.coordinator.lastCurrentLocation ?? emptyLocation) || context.coordinator.lastCurrentLocation == nil ||
                  context.coordinator.lastCurrentLocation!.lat != viewModel.currentLocation.lat ||
                  context.coordinator.lastCurrentLocation!.lng != viewModel.currentLocation.lng) {
                
                context.coordinator.lastCurrentLocation = viewModel.currentLocation
                NSLog("Current location updated to: \(viewModel.currentLocation.lat), \(viewModel.currentLocation.lng)")
                
                
                
                    animateCamera(mapView: mapView, to: viewModel.currentLocation, zoom: 15.0)
                    onCenterLocationConsumed?()
                
            }
            
            // Handle centerToCurrentLocation flag
            if viewModel.centerToCurrentLocation {
                NSLog("Center to current location requested")
                animateCamera(mapView: mapView, to: viewModel.currentLocation, zoom: 15.0)
                onCenterLocationConsumed?()
            }
            
            NSLog("Selected location updated to: \(viewModel.selectedLocation?.lat), \(viewModel.selectedLocation?.lng)")

            // Handle selected location changes
            if  viewModel.selectedLocation != context.coordinator.lastSelectedLocation {
                mapView.clear()
                addMarker(on: mapView, at: viewModel.selectedLocation ?? emptyLocation)
                animateCamera(mapView: mapView, to: viewModel.selectedLocation ?? emptyLocation, zoom: 15.0)
                context.coordinator.lastSelectedLocation = viewModel.selectedLocation
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
            marker.title = location.name.isEmpty ? "Seçilen Konum" : location.name
            marker.map = mapView
        }
        
        func makeCoordinator() -> Coordinator {
            Coordinator(self)
        }
        
        class Coordinator: NSObject, GMSMapViewDelegate {
            var parent: GoogleMapIosView
            var isInitialLoad = true
            var lastSelectedLocation: Location?
            var lastCurrentLocation: Location?
            
            init(_ parent: GoogleMapIosView) {
                self.parent = parent
                super.init()
            }
            
            func logMapLoaded() {
                NSLog("PlatformMap - iOS: Map loaded!")
            }
            
            func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
                let location = Location(name: "Seçilen Konum", lat: coordinate.latitude, lng: coordinate.longitude)
                mapView.clear()
                parent.addMarker(on: mapView, at: location)
                
                // Animate to selected location
                let cameraUpdate = GMSCameraUpdate.setTarget(coordinate, zoom: 15)
                mapView.animate(with: cameraUpdate)
                
                // Update last selected location
                lastSelectedLocation = location
                
                // Notify Kotlin side of selection
                parent.onLocationSelected?(location)
                NSLog("Location selected at: \(coordinate.latitude), \(coordinate.longitude)")
            }
            
            // Track map movement status to match Android's cameraPositionState.isMoving
            func mapView(_ mapView: GMSMapView, didChange position: GMSCameraPosition) {
                NSLog("MAP_STATE: Is Moving")
            }
            
            func mapView(_ mapView: GMSMapView, idleAt position: GMSCameraPosition) {
                NSLog("MAP_STATE: Is Idle")
            }
        }
    }
