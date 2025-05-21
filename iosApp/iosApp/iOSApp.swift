import SwiftUI
import GoogleMaps

@main
struct iOSApp: App {
    init(){
        GMSServices.provideAPIKey("AIzaSyDJ2CBHHBdyhWaIDaejYMwfGNGFSLxg5Ak")
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
