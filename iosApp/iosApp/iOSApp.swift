import SwiftUI
import GoogleMaps
import GooglePlaces


@main
struct iOSApp: App {
    init(){
        GMSServices.provideAPIKey("AIzaSyDJ2CBHHBdyhWaIDaejYMwfGNGFSLxg5Ak")
        GMSPlacesClient.provideAPIKey("AIzaSyDJ2CBHHBdyhWaIDaejYMwfGNGFSLxg5Ak")

    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
