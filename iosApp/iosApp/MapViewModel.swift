//
//  MapViewModel.swift
//  iosApp
//
//  Created by Yusuf Teker on 23.05.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

class MapViewModel: ObservableObject {
    @Published var currentLocation: Location
    @Published var selectedLocation: Location?
    @Published var centerToCurrentLocation: Bool
    
    init(currentLocation: Location, selectedLocation: Location?, centerToCurrentLocation: Bool) {
        self.currentLocation = currentLocation
        self.selectedLocation = selectedLocation
        self.centerToCurrentLocation = centerToCurrentLocation
    }
    
    // Bu fonksiyonlar Kotlin tarafından çağrılacak
    func updateCurrentLocation(_ location: Location) {
        DispatchQueue.main.async {
            NSLog("MapViewModel updateCurrentLocation")
            self.currentLocation = location
        }
    }
    
    func updateSelectedLocation(_ location: Location?) {
        DispatchQueue.main.async {
            NSLog("MapViewModel updateSelectedLocation")

            self.selectedLocation = location
        }
    }
    
    func updateCenterToCurrentLocation(_ shouldCenter: Bool) {
        DispatchQueue.main.async {
            NSLog("MapViewModel updateCenterToCurrentLocation")

            self.centerToCurrentLocation = shouldCenter
        }
    }
}
